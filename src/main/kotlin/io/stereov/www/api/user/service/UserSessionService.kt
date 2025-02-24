package io.stereov.www.api.user.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.stereov.www.api.user.exception.EmailAlreadyExistsException
import io.stereov.www.api.user.dto.LoginUserDto
import io.stereov.www.api.user.dto.RegisterUserDto
import io.stereov.www.api.auth.exception.AuthException
import io.stereov.www.api.auth.exception.InvalidCredentialsException
import io.stereov.www.api.auth.service.AuthenticationService
import io.stereov.www.api.global.service.email.EmailVerificationCooldownService
import io.stereov.www.api.global.service.email.MailService
import io.stereov.www.api.global.service.email.exception.EmailVerificationCooldownException
import io.stereov.www.api.global.service.hash.HashService
import io.stereov.www.api.global.service.jwt.JwtService
import io.stereov.www.api.properties.MailProperties
import io.stereov.www.api.user.dto.UserDto
import io.stereov.www.api.user.model.UserDocument
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class UserSessionService(
    private val userService: UserService,
    private val hashService: HashService,
    private val jwtService: JwtService,
    private val authenticationService: AuthenticationService,
    private val mailService: MailService,
    private val mailProperties: MailProperties,
    private val emailVerificationCooldownService: EmailVerificationCooldownService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun checkCredentialsAndGetUser(payload: LoginUserDto): UserDocument {
        logger.debug { "Logging in user ${payload.email}" }
        val user = userService.findByEmailOrNull(payload.email)
            ?: throw InvalidCredentialsException()

        if (!hashService.checkBcrypt(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        if (user.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        return user
    }

    suspend fun registerUserAndGetUserId(payload: RegisterUserDto): UserDocument {
        logger.debug { "Registering user ${payload.email}" }

        if (userService.existsByEmail(payload.email)) {
            throw EmailAlreadyExistsException("Failed to register user ${payload.email}")
        }

        val verificationUuid = UUID.randomUUID().toString()

        val userDocument = UserDocument(
            email = payload.email,
            password = hashService.hashBcrypt(payload.password),
            name = payload.name,
            verificationUuid = verificationUuid,
        )

        val savedUserDocument = userService.save(userDocument)

        if (savedUserDocument.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        if (mailProperties.enableEmailVerification) {
            mailService.sendVerificationEmail(savedUserDocument)
        }

        return savedUserDocument
    }

    suspend fun verifyEmail(token: String): UserDto {
        val verificationToken = jwtService.validateAndExtractVerificationToken(token)
        val user = userService.findByEmail(verificationToken.email)

        return if (user.verificationUuid == verificationToken.uuid) {
            userService.save(user.copy(emailVerified = true)).toDto()
        } else {
            user.toDto()
        }
    }

    suspend fun getRemainingEmailVerificationCooldown(): Long {
        val userId = authenticationService.getCurrentAccountId()
        return emailVerificationCooldownService.getRemainingEmailVerificationCooldown(userId)
    }

    suspend fun resendEmailVerificationToken() {
        val userId = authenticationService.getCurrentAccountId()
        val remainingCooldown = emailVerificationCooldownService.getRemainingEmailVerificationCooldown(userId)

        if (remainingCooldown > 0) throw EmailVerificationCooldownException(remainingCooldown)

        val user = userService.findById(userId)

        return mailService.sendVerificationEmail(user)
    }

    suspend fun logout(deviceId: String): UserDocument {
        val userId = authenticationService.getCurrentAccountId()
        val user = userService.findById(userId)
        val updatedDevices = user.devices.filterNot { it.id == deviceId }

        return userService.save(user.copy(devices = updatedDevices, lastActive = Instant.now()))
    }
}
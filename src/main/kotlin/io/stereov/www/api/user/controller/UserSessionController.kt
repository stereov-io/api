package io.stereov.www.api.user.controller

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.stereov.www.api.auth.service.AuthenticationService
import io.stereov.www.api.user.dto.*
import io.stereov.www.api.user.exception.UserException
import io.stereov.www.api.user.model.DeviceInfo
import io.stereov.www.api.user.model.UserDocument
import io.stereov.www.api.user.service.CookieService
import io.stereov.www.api.user.service.UserService
import io.stereov.www.api.user.service.UserSessionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/user")
class UserSessionController(
    private val authenticationService: AuthenticationService,
    private val userService: UserService,
    private val userSessionService: UserSessionService,
    private val cookieService: CookieService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/me")
    suspend fun getAccount(): ResponseEntity<UserDto> {
        val accountId = authenticationService.getCurrentAccountId()

        return ResponseEntity.ok(userService.findById(accountId).toDto())
    }

    @PostMapping("/login")
    suspend fun login(
        exchange: ServerWebExchange,
        @RequestBody payload: LoginUserDto
    ): ResponseEntity<UserDto> {
        logger.info { "Executing login" }

        val user = userSessionService.checkCredentialsAndGetUser(payload)

        return generateTokensAndLogin(exchange, user, payload.device)
    }

    @PostMapping("/register")
    suspend fun register(
        exchange: ServerWebExchange,
        @RequestBody @Valid payload: RegisterUserDto
    ): ResponseEntity<UserDto> {
        logger.info { "Executing register" }

        val userId = userSessionService.registerUserAndGetUserId(payload)

        return generateTokensAndLogin(exchange, userId, payload.device)
    }

    private suspend fun generateTokensAndLogin(
        exchange: ServerWebExchange,
        user: UserDocument,
        deviceInfo: DeviceInfoRequestDto,
    ): ResponseEntity<UserDto> {
        val userId = user.id ?: throw UserException("No ID found in user document")

        val ipAddress = exchange.request.remoteAddress?.address?.hostAddress

        val accessTokenCookie = cookieService.createAccessTokenCookie(userId)
        val refreshTokenCookie = cookieService.createRefreshTokenCookie(userId, deviceInfo, ipAddress)

        return ResponseEntity.ok()
            .header("Set-Cookie", accessTokenCookie.toString())
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(user.toDto())
    }

    @GetMapping("/verify-email")
    suspend fun verifyEmail(@RequestParam token: String): ResponseEntity<UserDto> {
        val authInfo = userSessionService.verifyEmail(token)

        return ResponseEntity.ok()
            .body(authInfo)
    }

    @GetMapping("/email-verification-cooldown")
    suspend fun getRemainingEmailVerificationCooldown(): ResponseEntity<Map<String, Long>> {
        val remainingCooldown = userSessionService.getRemainingEmailVerificationCooldown()

        return ResponseEntity.ok().body(mapOf(
            "remaining" to remainingCooldown
        ))
    }

    @PostMapping("/resend-verification-email")
    suspend fun resendVerificationEmail(): ResponseEntity<Map<String, String>> {

        userSessionService.resendEmailVerificationToken()

        return ResponseEntity.ok().body(
            mapOf("message" to "Successfully resend verification email")
        )
    }

    @GetMapping("/devices")
    suspend fun getDevices(): ResponseEntity<Map<String, List<DeviceInfo>>> {
        val userId = authenticationService.getCurrentAccountId()

        val devices = userService.getDevices(userId)

        return ResponseEntity.ok(
            mapOf("devices" to devices)
        )
    }

    @DeleteMapping("/devices")
    suspend fun deleteDevice(@RequestParam("device_id") deviceId: String): ResponseEntity<Map<String, List<DeviceInfoResponseDto>>> {
        val userId = authenticationService.getCurrentAccountId()

        val updatedUser = userService.deleteDevice(userId, deviceId)

        return ResponseEntity.ok(
            mapOf("devices" to updatedUser.devices.map { it.toResponseDto() })
        )
    }
}
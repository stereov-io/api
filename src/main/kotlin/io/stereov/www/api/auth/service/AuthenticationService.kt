package io.stereov.www.api.auth.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import io.stereov.www.api.auth.exception.InvalidPrincipalException
import io.stereov.www.api.auth.model.CustomAuthenticationToken
import io.stereov.www.api.global.service.jwt.exception.InvalidTokenException
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service

@Service
class AuthenticationService {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getCurrentAuth(): CustomAuthenticationToken {
        logger.debug { "Extracting AuthInfo" }

        val auth = getCurrentAuthentication()
        return auth
    }

    suspend fun getCurrentAccountId(): String {
        logger.debug {"Extracting user ID." }

        val auth = getCurrentAuthentication()
        return auth.accountId
    }

    private suspend fun getCurrentAuthentication(): CustomAuthenticationToken {
        val securityContext: SecurityContext = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
            ?: throw InvalidPrincipalException("No security context found.")

        val authentication = securityContext.authentication
            ?: throw InvalidTokenException("Authentication is missing.")

        return authentication as? CustomAuthenticationToken
                ?: throw InvalidTokenException("Authentication does not contain needed properties")
    }
}
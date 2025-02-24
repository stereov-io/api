package io.stereov.www.api.filter

import io.stereov.www.api.auth.model.CustomAuthenticationToken
import io.stereov.www.api.global.service.jwt.JwtService
import io.stereov.www.api.global.service.jwt.exception.TokenException
import io.stereov.www.api.user.service.UserService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import io.stereov.www.api.config.Constants

class CookieAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain) = mono {

        val authToken = extractTokenFromRequest(exchange)

        if (!authToken.isNullOrBlank()) {
            val accessToken = try {
                jwtService.validateAndExtractAccessToken(authToken)
            } catch(e: TokenException) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED

                return@mono exchange.response.setComplete().awaitFirstOrNull()
            }

            val account = userService.findByIdOrNull(accessToken.accountId)

            if (account == null) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED

                return@mono exchange.response.setComplete().awaitFirstOrNull()
            }

            val authentication = CustomAuthenticationToken(account)

            val securityContext = SecurityContextImpl(authentication)
            return@mono chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .awaitFirstOrNull()
        }

        chain.filter(exchange).awaitFirstOrNull()
    }

    private fun extractTokenFromRequest(exchange: ServerWebExchange): String? {
        return exchange.request.cookies[Constants.ACCESS_TOKEN_COOKIE]?.firstOrNull()?.value
    }
}
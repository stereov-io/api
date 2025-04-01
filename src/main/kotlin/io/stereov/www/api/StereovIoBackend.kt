package io.stereov.www.api

import io.stereov.web.global.service.jwt.JwtService
import io.stereov.web.properties.JwtProperties
import io.stereov.web.user.service.token.TwoFactorAuthTokenService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class StereovIoBackend

@Bean
fun twoFactorAuthTokenService(jwtService: JwtService, jwtProperties: JwtProperties): TwoFactorAuthTokenService = TwoFactorAuthTokenService(jwtService, jwtProperties)

fun main(args: Array<String>) {
    runApplication<StereovIoBackend>(*args)

}

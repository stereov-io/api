package io.stereov.www.api.global.service.jwt.model

data class EmailVerificationToken(
    val email: String,
    val uuid: String,
)

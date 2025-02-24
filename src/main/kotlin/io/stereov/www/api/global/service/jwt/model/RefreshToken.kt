package io.stereov.www.api.global.service.jwt.model

data class RefreshToken(
    val accountId: String,
    val deviceId: String,
    val value: String,
)

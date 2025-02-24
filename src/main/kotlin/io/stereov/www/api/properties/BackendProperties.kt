package io.stereov.www.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "backend")
data class BackendProperties(
    val baseUrl: String,
    val secure: Boolean,
    val ipRateLimitMinute: Long,
    val accountRateLimitMinute: Long,
)

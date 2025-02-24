package io.stereov.www.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    val baseUrl: String,
    val emailVerificationPath: String,
)
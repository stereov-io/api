package io.stereov.www.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail")
data class MailProperties(
    val enableEmailVerification: Boolean,
    val host: String,
    val port: Int,
    val email: String,
    val username: String,
    val password: String,
    val transportProtocol: String,
    val smtpAuth: Boolean,
    val smtpStarttls: Boolean,
    val debug: Boolean,
    val verificationExpiration: Long,
    val verificationSendCooldown: Long,
)
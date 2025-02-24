package io.stereov.www.api.config

import io.stereov.www.api.properties.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    JwtProperties::class,
    EncryptionProperties::class,
    BackendProperties::class,
    FrontendProperties::class,
    MailProperties::class,
)
class ApplicationConfig
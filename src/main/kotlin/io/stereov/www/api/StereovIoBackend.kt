package io.stereov.www.api

import io.stereov.web.properties.MailProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(MailProperties::class)
class StereovIoBackend

fun main(args: Array<String>) {
    runApplication<StereovIoBackend>(*args)
}

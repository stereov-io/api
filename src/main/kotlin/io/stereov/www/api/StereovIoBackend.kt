package io.stereov.www.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StereovIoBackend

fun main(args: Array<String>) {
    runApplication<StereovIoBackend>(*args)
}

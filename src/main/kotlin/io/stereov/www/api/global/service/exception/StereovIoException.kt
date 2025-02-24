package io.stereov.www.api.global.service.exception

open class StereovIoException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
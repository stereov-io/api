package io.stereov.www.api.auth.exception

import io.stereov.www.api.global.service.exception.StereovIoException


open class AuthException(
    message: String,
    cause: Throwable? = null
) : StereovIoException(
    message,
    cause
)
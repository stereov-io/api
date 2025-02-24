package io.stereov.www.api.global.service.jwt.exception

import io.stereov.www.api.global.service.exception.StereovIoException

open class TokenException(message: String, cause: Throwable? = null) : StereovIoException(message, cause)
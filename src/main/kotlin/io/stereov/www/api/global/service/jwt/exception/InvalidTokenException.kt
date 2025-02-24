package io.stereov.www.api.global.service.jwt.exception

class InvalidTokenException(message: String, cause: Throwable? = null) : TokenException(message, cause)

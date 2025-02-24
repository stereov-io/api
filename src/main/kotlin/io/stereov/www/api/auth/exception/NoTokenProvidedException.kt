package io.stereov.www.api.auth.exception

class NoTokenProvidedException(message: String, cause: Throwable? = null) : AuthException(message, cause)

package io.stereov.www.api.user.exception

open class UserException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
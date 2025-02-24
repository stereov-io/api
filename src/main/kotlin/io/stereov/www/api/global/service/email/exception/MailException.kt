package io.stereov.www.api.global.service.email.exception

import io.stereov.www.api.global.service.exception.StereovIoException

open class MailException(message: String, cause: Throwable? = null) : StereovIoException(message, cause)
package io.stereov.www.api.global.service.email.exception

class EmailVerificationCooldownException(remainingCooldown: Long, cause: Throwable? = null) : MailException(
    message = "Please wait $remainingCooldown seconds before requesting another verification email", cause)
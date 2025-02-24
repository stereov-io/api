package io.stereov.www.api.auth.exception

class InvalidCredentialsException : AuthException(
    message = "Login failed: Invalid credentials",
)
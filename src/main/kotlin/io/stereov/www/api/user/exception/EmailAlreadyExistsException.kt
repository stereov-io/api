package io.stereov.www.api.user.exception

class EmailAlreadyExistsException(info: String) : UserException(
    message = "$info: Email already exists"
)
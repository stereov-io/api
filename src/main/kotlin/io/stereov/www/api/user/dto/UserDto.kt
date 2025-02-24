package io.stereov.www.api.user.dto

import io.stereov.www.api.user.model.DeviceInfo
import io.stereov.www.api.user.model.Role
import java.time.Instant

data class UserDto(
    val id: String? = null,
    val name: String,
    val email: String,
    val roles: List<Role> = listOf(Role.USER),
    val emailVerified: Boolean = false,
    val devices: List<DeviceInfo> = listOf(),
    val lastActive: Instant = Instant.now(),
)
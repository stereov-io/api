package io.stereov.www.api.user.dto

data class DeviceInfoRequestDto(
    val id: String,
    val browser: String? = null,
    val os: String? = null,
)

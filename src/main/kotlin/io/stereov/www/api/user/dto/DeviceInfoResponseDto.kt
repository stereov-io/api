package io.stereov.www.api.user.dto

import io.stereov.www.api.user.model.DeviceInfo
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoResponseDto(
    val id: String,
    val browser: String? = null,
    val os: String? = null,
    val ipAddress: String?,
    val location: DeviceInfo.LocationInfo?,
)
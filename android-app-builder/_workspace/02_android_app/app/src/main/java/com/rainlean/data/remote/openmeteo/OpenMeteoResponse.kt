package com.rainlean.data.remote.openmeteo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    @SerialName("current")
    val current: CurrentWeather
)

@Serializable
data class CurrentWeather(
    @SerialName("time")
    val time: String,
    @SerialName("precipitation")
    val precipitation: Double,
    @SerialName("wind_direction_10m")
    val windDirection10m: Double,
    @SerialName("wind_speed_10m")
    val windSpeed10m: Double
)


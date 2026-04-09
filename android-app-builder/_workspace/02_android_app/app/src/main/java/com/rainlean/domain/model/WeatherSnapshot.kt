package com.rainlean.domain.model

data class WeatherSnapshot(
    val precipitationMmPerHour: Double,
    val windDirectionFromDeg: Double,
    val windSpeedMps: Double,
    val observedAtEpochSec: Long,
    val source: WeatherSource
)

enum class WeatherSource {
    KMA,
    OPEN_METEO,
    CACHE
}


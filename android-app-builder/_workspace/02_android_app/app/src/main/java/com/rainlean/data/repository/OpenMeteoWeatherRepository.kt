package com.rainlean.data.repository

import com.rainlean.data.remote.openmeteo.OpenMeteoApiService
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import javax.inject.Inject

class OpenMeteoWeatherRepository @Inject constructor(
    private val api: OpenMeteoApiService
) : WeatherRepository {
    override suspend fun getNow(lat: Double, lon: Double): WeatherSnapshot {
        val response = api.getCurrentWeather(latitude = lat, longitude = lon)
        return WeatherSnapshot(
            precipitationMmPerHour = response.current.precipitation,
            windDirectionFromDeg = response.current.windDirection10m,
            windSpeedMps = response.current.windSpeed10m,
            observedAtEpochSec = System.currentTimeMillis() / 1000,
            source = WeatherSource.OPEN_METEO
        )
    }
}


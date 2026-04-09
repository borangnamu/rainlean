package com.rainlean.data.repository

import com.rainlean.BuildConfig
import com.rainlean.core.weather.KmaBaseTimeResolver
import com.rainlean.core.weather.KmaGridConverter
import com.rainlean.data.remote.kma.KmaApiService
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import javax.inject.Inject

class KmaWeatherRepository @Inject constructor(
    private val api: KmaApiService
) : WeatherRepository {
    override suspend fun getNow(lat: Double, lon: Double): WeatherSnapshot {
        val serviceKey = BuildConfig.KMA_SERVICE_KEY
        require(serviceKey.isNotBlank()) { "KMA_SERVICE_KEY is missing" }

        val grid = KmaGridConverter.latLonToGrid(lat, lon)
        val base = KmaBaseTimeResolver.resolve()
        val response = api.getUltraShortNowcast(
            serviceKey = serviceKey,
            baseDate = base.date,
            baseTime = base.time,
            nx = grid.nx,
            ny = grid.ny
        )

        val map = response.response.body.items.item.associateBy({ it.category }, { it.value })
        val rain = map["RN1"]?.toDoubleOrNull() ?: 0.0
        val windDirection = map["VEC"]?.toDoubleOrNull() ?: 0.0
        val windSpeed = map["WSD"]?.toDoubleOrNull() ?: 0.0

        return WeatherSnapshot(
            precipitationMmPerHour = rain,
            windDirectionFromDeg = windDirection,
            windSpeedMps = windSpeed,
            observedAtEpochSec = System.currentTimeMillis() / 1000,
            source = WeatherSource.KMA
        )
    }
}


package com.rainlean.data.repository

import com.rainlean.BuildConfig
import com.rainlean.domain.model.WeatherSnapshot
import javax.inject.Inject

class CompositeWeatherRepository @Inject constructor(
    private val kma: KmaWeatherRepository,
    private val openMeteo: OpenMeteoWeatherRepository
) : WeatherRepository {
    override suspend fun getNow(lat: Double, lon: Double): WeatherSnapshot {
        if (BuildConfig.KMA_SERVICE_KEY.isNotBlank()) {
            runCatching { return kma.getNow(lat, lon) }
        }
        return openMeteo.getNow(lat, lon)
    }
}


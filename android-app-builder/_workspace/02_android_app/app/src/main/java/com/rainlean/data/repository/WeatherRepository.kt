package com.rainlean.data.repository

import com.rainlean.domain.model.WeatherSnapshot

interface WeatherRepository {
    suspend fun getNow(lat: Double, lon: Double): WeatherSnapshot
}


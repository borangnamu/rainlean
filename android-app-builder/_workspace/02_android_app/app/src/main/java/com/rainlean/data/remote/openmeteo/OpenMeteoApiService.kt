package com.rainlean.data.remote.openmeteo

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "precipitation,wind_direction_10m,wind_speed_10m",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("timezone") timezone: String = "Asia/Seoul"
    ): OpenMeteoResponse
}


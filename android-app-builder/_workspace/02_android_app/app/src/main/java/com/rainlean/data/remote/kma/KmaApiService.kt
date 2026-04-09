package com.rainlean.data.remote.kma

import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {
    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
    suspend fun getUltraShortNowcast(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): KmaUltraShortNcstResponse
}

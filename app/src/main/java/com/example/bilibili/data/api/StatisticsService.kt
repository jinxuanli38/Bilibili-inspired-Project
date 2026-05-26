package com.example.bilibili.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface StatisticsService {
    @POST("statistics/getActualTimeStatisticsInfo")
    suspend fun getActualTimeStatisticsInfo(): String

    @POST("statistics/getWeekStatisticsInfo")
    @FormUrlEncoded
    suspend fun getWeekStatisticsInfo(
        @Field("dataType") dataType: Int,
    ): String
}

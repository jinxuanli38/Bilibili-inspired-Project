package com.example.bilibili.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserActionService {
    // 对视频的交互操作
    @POST("userAction/doAction")
    @FormUrlEncoded
    suspend fun doAction(
        @Field("videoId") videoId: String,
        @Field("actionType") actionType: Int,
        @Field("actionCount") actionCount: Int? = null
    ): String
}
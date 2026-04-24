package com.example.bilibili.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface VideoService {
    // 获取视频信息
    @POST("video/getVideoInfo")
    @FormUrlEncoded
    suspend fun getVideoInfo(
        @Field("videoId") videoId: String
    ): String

    // 获得对应的视频列表
    @POST("video/loadVideo")
    @FormUrlEncoded
    suspend fun loadVideo(
        @Field("pageNo") pageNo: Int? = null,
        @Field("categoryId") categoryId: Int? = null,
        @Field("pCategoryId") pCategoryId: Int? = null
    ): String

    // 获取分片列表接口
    @POST("video/loadVideoPList")
    @FormUrlEncoded
    suspend fun loadVideoPList(
        @Field("videoId") videoId: String
    ): String
}
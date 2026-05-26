package com.example.bilibili.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UcenterService {
    @POST("ucenter/post/loadVideoList")
    @FormUrlEncoded
    suspend fun loadPostVideoList(
        @Field("status") status: Int? = null,
        @Field("pageNo") pageNo: Int,
        @Field("videoNameFuzzy") videoNameFuzzy: String? = null,
    ): String

    @POST("ucenter/post/delVideo")
    @FormUrlEncoded
    suspend fun delVideo(@Field("videoId") videoId: String): String

    @POST("ucenter/interaction/loadAllVideo")
    suspend fun loadAllVideo(): String

    @POST("ucenter/interaction/loadComment")
    @FormUrlEncoded
    suspend fun loadComment(
        @Field("pageNo") pageNo: Int,
        @Field("videoId") videoId: String? = null,
    ): String

    @POST("ucenter/interaction/delComment")
    @FormUrlEncoded
    suspend fun delComment(@Field("commentId") commentId: Int): String

    @POST("ucenter/interaction/loadDanmu")
    @FormUrlEncoded
    suspend fun loadDanmu(
        @Field("pageNo") pageNo: Int,
        @Field("videoId") videoId: String? = null,
    ): String

    @POST("ucenter/interaction/delDanmu")
    @FormUrlEncoded
    suspend fun delDanmu(@Field("danmuId") danmuId: Int): String
}

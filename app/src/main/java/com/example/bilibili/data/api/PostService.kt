package com.example.bilibili.data.api

import retrofit2.http.*

interface PostService {
    // 投稿视频接口
    @POST("ucenter/post/postVideo")
    @FormUrlEncoded
    suspend fun postVideo(
        @Field("videoId") videoId: String? = null,
        @Field("videoCover") videoCover: String,
        @Field("videoName") videoName: String,
        @Field("pCategoryId") pCategoryId: Int,
        @Field("categoryId") categoryId: Int?,
        @Field("postType") postType: Int,
        @Field("originInfo") originInfo: String? = null,
        @Field("tags") tags: String,
        @Field("introduction") introduction: String?,
        @Field("interaction") interaction: String?,
        @Field("uploadFileList") uploadFileList: String
    ): String

    // 个人信息接口
    @POST("ucenter/home/getUserInfo")
    @FormUrlEncoded
    suspend fun getUserInfo(
        @Field("userId") userId: String
    ): String

    // 获取收藏列表
    @POST("ucenter/home/loadUserCollection")
    @FormUrlEncoded
    suspend fun loadUserCollection(
        @Field("userId") userId: String,
        @Field("pageNo") pageNo: Int? = null
    ): String

    // 加载当前用户发布的视频列表
    @POST("ucenter/home/loadVideoList")
    @FormUrlEncoded
    suspend fun loadVideoList(
        @Field("userId") userId: String,
        @Field("type") type: Int? = null,
        @Field("pageNo") pageNo: Int? = null,
        @Field("orderType") orderType: Int? = null,
        @Field("videoName") videoName: String? = null,
    ): String

    // 加载用户关注数
    @POST("ucenter/home/loadFocusList")
    @FormUrlEncoded
    suspend fun loadFocusList(
        @Field("pageNo") pageNo: Int
    ): String

    // 加载用户粉丝数
    @POST("ucenter/home/loadFansList")
    @FormUrlEncoded
    suspend fun loadFansList(
        @Field("pageNo") pageNo: Int
    ): String

    // 关注用户
    @POST("ucenter/home/focus")
    @FormUrlEncoded
    suspend fun focus(
        @Field("focusUserId") focusUserId: String
    ): String

    // 取消关注
    @POST("ucenter/home/cancelFocus")
    @FormUrlEncoded
    suspend fun cancelFocus(
        @Field("focusUserId") focusUserId: String
    ): String

    @POST("ucenter/home/removeFan")
    @FormUrlEncoded
    suspend fun removeFan(
        @Field("fanUserId") fanUserId: String
    ): String

    // 更新用户信息
    @POST("ucenter/home/updateUserInfo")
    @FormUrlEncoded
    suspend fun updateUserInfo(
        @Field("avatar") avatar: String,
        @Field("nickName") nickName: String,
        @Field("sex") sex: Int,
        @Field("birthday") birthday: String,
        @Field("school") school: String,
        @Field("noticeInfo") noticeInfo: String,
        @Field("personalIntroduction") personalIntroduction: String
    ): String
}

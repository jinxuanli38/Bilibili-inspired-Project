package com.example.bilibili.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface AccountService {
    /**
     * 获取验证码的原始 JSON 字符串
     */
    @POST("account/checkCode")
    suspend fun getCaptcha(): String

    /**
     * 注册账号
     */
    @FormUrlEncoded
    @POST("account/register")
    suspend fun register(
        @Field("email") email: String,
        @Field("nickName") nickName: String,
        @Field("registerPassword") registerPassword: String,
        @Field("checkCode") checkCode: String,
        @Field("checkCodeKey") checkCodeKey: String
    ): String

    /**
     * 登录功能
     */
    @FormUrlEncoded
    @POST("account/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("checkCode") checkCode: String,
        @Field("checkCodeKey") checkCodeKey: String
    ): String

    /**
     * 自动登录功能
     */
    @POST("account/autoLogin")
    suspend fun autoLogin(
        @Header("webToken") webToken: String
    ): String
}
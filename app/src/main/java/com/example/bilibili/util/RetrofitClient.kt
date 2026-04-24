package com.example.bilibili.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://192.168.21.129:7071/"

    // Token 拦截器 - 自动添加 token 到请求头
    private val tokenInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = SPUtils.getToken()

        // 如果有 token，添加到请求头
        val requestWithToken = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("webToken", token)  // 与服务端约定的 token 名称
                .build()
        } else {
            originalRequest
        }

        // 传递到下一个拦截器
        chain.proceed(requestWithToken)
    }

    // 配置 OkHttp（用于设置超时和日志打印）
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)  // 先添加 token 拦截器
        .addInterceptor(HttpLoggingInterceptor().apply {
            // 打印请求和响应的详细日志
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // 初始化 Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        // 用 Scalars 转换器来接收原始String
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)
}
package com.example.bilibili.data.api

import retrofit2.http.POST

interface CategoryInfoService {
    @POST("categoryInfo/loadAllCategoryInfo")
    suspend fun loadAllCategoryInfo(): String
}
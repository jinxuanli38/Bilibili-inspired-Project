package com.example.bilibili.data.model

// 只要这两个，其他的通通不写，后端返回再多字段也会被你忽略
data class CategoryInfo(
    val categoryId: Int,           // 一级分类ID (对应后端pCategoryId)
    val categoryName: String,      // 分类名称
    val subCategoryId: Int? = null, // 二级分类ID (对应后端categoryId)
    val subCategoryName: String? = null // 二级分类名称
)
package com.example.bilibili.data.model

data class VideoResource(
    val fileId: String,
    val userId: String,
    val videoId: String,
    val fileName: String,
    val fileIndex: Int,
    val filePath: String,
    val duration: Int
)
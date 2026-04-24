package com.example.bilibili.data.model

data class VideoUploadInfo(
    var uploadId: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val duration: Long, // 毫秒
    val chunks: Int,
    var currentChunk: Int = 0
)
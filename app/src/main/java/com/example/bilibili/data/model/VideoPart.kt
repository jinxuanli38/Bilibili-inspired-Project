package com.example.bilibili.data.model

data class VideoPart(
    val id: String = System.currentTimeMillis().toString(), // 唯一ID
    val videoPath: String,      // 视频文件路径
    val title: String = "",     // 视频标题
    val duration: Long = 0L,   // 视频时长（毫秒）
    val coverPath: String? = null, // 封面图片路径
    val uploadId: String = "", // 上传ID
    val uploadStatus: Int = 0, // 0:未上传, 1:上传中, 2:上传完成
    val uploadProgress: Int = 0  // 上传进度 0-100
)
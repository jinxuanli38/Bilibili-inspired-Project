package com.example.bilibili.data.model

/**
 * 视频文件信息 - 用于投稿接口
 */
data class VideoInfoFilePost(
    val uploadId: String = "",         // 上传ID
    val fileName: String = "",         // 文件名
    val fileSize: Long = 0L,           // 文件大小
    val duration: Int = 0,             // 视频时长（秒）
    val fileMd5: String = ""           // 文件MD5（可选）
)
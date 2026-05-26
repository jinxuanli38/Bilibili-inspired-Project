package com.example.bilibili.data.model

data class CreatorVideoPost(
    val videoId: String,
    val videoName: String,
    val videoCover: String,
    val status: Int,
    val statusLabel: String,
    val playCount: Int,
    val createTime: String,
)

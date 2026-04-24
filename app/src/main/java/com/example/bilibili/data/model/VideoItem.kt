package com.example.bilibili.data.model

data class VideoItem(
    val videoId: String,
    val videoName: String,     // 对应 JSON 中的 videoName
    val videoCover: String,    // 对应 JSON 中的 videoCover
    val playCount: Int,        // 注意：JSON 中是 Int
    val commentCount: Int,     // 注意：JSON 中是 Int
    val duration: Int,         // 注意：JSON 中是 Int
    val createTime: String,
    val danmuCount: Int,        // 如果你后续要在封面显示弹幕数
    val nickName: String
)
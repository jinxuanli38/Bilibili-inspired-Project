package com.example.bilibili.data.model

data class VideoDetail(
    val videoId: String,
    val videoName: String,
    val videoCover: String,
    val introduction: String,
    val createTime: String,
    val playCount: Int,
    val likeCount: Int,
    val coinCount: Int,
    val collectCount: Int,
    val danmuCount: Int // 虽然你不需要显示列表，但可能需要显示“共1条弹幕”的数量
)
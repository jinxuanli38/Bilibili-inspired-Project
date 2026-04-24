package com.example.bilibili.data.model

data class CollectVideo(
    val actionId: Int,
    val videoId: String,
    val videoUserId: String,
    val commentId: Int,
    val actionType: Int,
    val actionCount: Int,
    val userId: String,
    val actionTime: String,
    val videoCover: String,
    val videoName: String
)
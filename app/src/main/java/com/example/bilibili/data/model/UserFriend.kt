package com.example.bilibili.data.model

data class UserFriend(
    val userId: String,          // 我自己的ID
    val otherUserId: String,     // 对方的ID
    val otherNickName: String,   // 对方昵称
    val otherAvatar: String,     // 对方头像
    val otherPersonalIntroduction: String?, // 个人简介（关注列表）
    val focusType: Int,
    val focusTime: String,
    val otherFansCount: Int = 0,
    val otherVideoCount: Int = 0,
)
package com.example.bilibili.data.model

data class UserFriend(
    val userId: String,          // 我自己的ID
    val otherUserId: String,     // 对方的ID
    val otherNickName: String,   // 对方昵称
    val otherAvatar: String,     // 对方头像
    val otherPersonalIntroduction: String?, // 个人简介
    val focusType: Int,          // 关注类型：通常 1 表示互相关注，或者特定的状态
    val focusTime: String        // 关注时间
)
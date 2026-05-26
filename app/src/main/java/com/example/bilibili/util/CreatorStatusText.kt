package com.example.bilibili.util

object CreatorStatusText {
    fun videoStatusLabel(status: Int): String = when (status) {
        0 -> "转码中"
        1 -> "转码失败"
        2 -> "待审核"
        3 -> "审核通过"
        4 -> "审核不通过"
        else -> "未知"
    }
}

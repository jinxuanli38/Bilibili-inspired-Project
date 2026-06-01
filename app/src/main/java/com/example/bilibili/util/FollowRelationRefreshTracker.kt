package com.example.bilibili.util

import com.example.bilibili.ui.user.FollowStatsCenter

/**
 * 在 onPause 记录 [FollowStatsCenter.relationVersion]，onResume 若已变化则触发刷新。
 * 解决从他人主页返回后列表/个人页未更新的问题。
 */
class FollowRelationRefreshTracker {

    private var versionWhenPaused = FollowStatsCenter.relationVersion.value

    fun sync() {
        versionWhenPaused = FollowStatsCenter.relationVersion.value
    }

    fun onPause() {
        versionWhenPaused = FollowStatsCenter.relationVersion.value
    }

    fun onResumeIfChanged(onChanged: () -> Unit) {
        val current = FollowStatsCenter.relationVersion.value
        if (current != versionWhenPaused) {
            versionWhenPaused = current
            onChanged()
        }
    }
}

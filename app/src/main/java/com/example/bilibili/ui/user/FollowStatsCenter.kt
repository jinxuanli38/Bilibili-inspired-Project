package com.example.bilibili.ui.user

import com.example.bilibili.util.SPUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * 关注/粉丝数：本机乐观更新 + SSE 与服务端对齐。
 */
object FollowStatsCenter {

    data class FollowChange(
        val targetUserId: String,
        val followed: Boolean,
        val selfUserId: String = SPUtils.getUserId(),
    )

    data class CountUpdate(val userId: String, val count: Int)

    private val _changes = MutableSharedFlow<FollowChange>(extraBufferCapacity = 16)
    val changes: SharedFlow<FollowChange> = _changes.asSharedFlow()

    private val _fansCountUpdates = MutableSharedFlow<CountUpdate>(extraBufferCapacity = 16)
    val fansCountUpdates: SharedFlow<CountUpdate> = _fansCountUpdates.asSharedFlow()

    private val _focusCountUpdates = MutableSharedFlow<CountUpdate>(extraBufferCapacity = 16)
    val focusCountUpdates: SharedFlow<CountUpdate> = _focusCountUpdates.asSharedFlow()

    private val _selfFansCount = MutableStateFlow<Int?>(null)
    val selfFansCount = _selfFansCount.asStateFlow()

    private val _selfFocusCount = MutableStateFlow<Int?>(null)
    val selfFocusCount = _selfFocusCount.asStateFlow()

    /** 关注关系变更序号；页面 onPause 记录、onResume 对比，避免后台错过 SharedFlow 事件。 */
    private val _relationVersion = MutableStateFlow(0)
    val relationVersion = _relationVersion.asStateFlow()

    fun publishLocalChange(change: FollowChange) {
        _changes.tryEmit(change)
        _relationVersion.value = _relationVersion.value + 1
    }

    fun applySsePayload(json: JSONObject) {
        when (json.optString("event")) {
            "fans_count" -> {
                val userId = json.optString("userId")
                val count = json.optInt("fansCount", -1)
                if (userId.isNotBlank() && count >= 0) {
                    val update = CountUpdate(userId, count)
                    _fansCountUpdates.tryEmit(update)
                    if (userId == SPUtils.getUserId()) {
                        _selfFansCount.value = count
                    }
                }
            }
            "focus_count" -> {
                val userId = json.optString("userId")
                val count = json.optInt("focusCount", -1)
                if (userId.isNotBlank() && count >= 0) {
                    val update = CountUpdate(userId, count)
                    _focusCountUpdates.tryEmit(update)
                    if (userId == SPUtils.getUserId()) {
                        _selfFocusCount.value = count
                    }
                }
            }
        }
    }

    fun seedSelfCounts(fansCount: Int, focusCount: Int) {
        _selfFansCount.value = fansCount
        _selfFocusCount.value = focusCount
    }

    fun adjustFansCount(current: Int, delta: Int): Int = (current + delta).coerceAtLeast(0)

    fun adjustFocusCount(current: Int, delta: Int): Int = (current + delta).coerceAtLeast(0)

    fun deltaForFollowChange(change: FollowChange, displayedUserId: String): Int? {
        val selfId = SPUtils.getUserId()
        if (selfId.isEmpty()) return null
        val delta = if (change.followed) 1 else -1
        return when {
            change.targetUserId == displayedUserId -> delta
            change.selfUserId == selfId && change.targetUserId == displayedUserId -> delta
            else -> null
        }
    }

    fun focusDeltaForSelf(change: FollowChange): Int? {
        val selfId = SPUtils.getUserId()
        if (selfId.isEmpty() || change.selfUserId != selfId) return null
        if (change.targetUserId == selfId) return null
        return if (change.followed) 1 else -1
    }

    fun fansDeltaForSelf(change: FollowChange): Int? {
        val selfId = SPUtils.getUserId()
        if (selfId.isEmpty()) return null
        if (change.targetUserId != selfId) return null
        return if (change.followed) 1 else -1
    }
}

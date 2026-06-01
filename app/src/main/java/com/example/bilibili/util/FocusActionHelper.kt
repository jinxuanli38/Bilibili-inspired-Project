package com.example.bilibili.util

import com.example.bilibili.data.api.PostService
import com.example.bilibili.ui.user.FollowStatsCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object FocusActionHelper {

    suspend fun setFocus(
        service: PostService,
        targetUserId: String,
        follow: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        if (targetUserId.isBlank()) return@withContext false
        try {
            val raw = if (follow) {
                service.focus(targetUserId)
            } else {
                service.cancelFocus(targetUserId)
            }
            val ok = JSONObject(raw).optInt("code") == 200
            if (ok) {
                FollowStatsCenter.publishLocalChange(
                    FollowStatsCenter.FollowChange(
                        targetUserId = targetUserId,
                        followed = follow,
                    ),
                )
            }
            ok
        } catch (_: Exception) {
            false
        }
    }
}

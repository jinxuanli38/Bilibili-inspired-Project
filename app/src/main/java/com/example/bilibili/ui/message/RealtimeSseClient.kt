package com.example.bilibili.ui.message

import android.os.Handler
import android.os.Looper
import com.example.bilibili.ui.user.FollowStatsCenter
import com.example.bilibili.util.SPUtils
import org.json.JSONObject

/**
 * 全局 SSE：消息未读 + 关注/粉丝数推送。引用计数，Application 与消息页可共用连接。
 */
object RealtimeSseClient {

    private val client = MessageSseClient()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var refCount = 0

    @Synchronized
    fun acquire() {
        if (SPUtils.getToken().isEmpty()) return
        if (refCount++ == 0) {
            client.start(::dispatchPayload)
        }
    }

    @Synchronized
    fun release() {
        if (refCount <= 0) return
        if (--refCount == 0) {
            client.stop()
        }
    }

    @Synchronized
    fun restart() {
        client.stop()
        refCount = 0
        acquire()
    }

    @Synchronized
    fun forceStop() {
        refCount = 0
        client.stop()
    }

    private fun dispatchPayload(payload: JSONObject) {
        mainHandler.post {
            MessageUnreadCenter.applySsePayload(payload)
            FollowStatsCenter.applySsePayload(payload)
        }
    }
}

package com.example.bilibili

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.bilibili.ui.message.RealtimeSseClient
import com.example.bilibili.ui.playVideo.BiliExo2PlayerManager
import com.example.bilibili.util.SPUtils
import com.example.bilibili.util.TextSelectHandleHelper
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType

class BiliBiliApplication : Application() {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        SPUtils.init(this)
        if (SPUtils.getToken().isNotEmpty()) {
            RealtimeSseClient.acquire()
        }
        // 默认屏幕适配，不拉伸比例
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        // m3u8 使用 ExoPlayer（Media3），HLS seek/时长比默认 IJK 更稳
        PlayerFactory.setPlayManager(BiliExo2PlayerManager::class.java)
        // 生命周期检测器
        registerActivityLifecycleCallbacks(EditTextHandleLifecycleCallbacks())
    }

    /**
     * 文字选择器
     */
    private class EditTextHandleLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            TextSelectHandleHelper.applyPinkHandlesIn(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}

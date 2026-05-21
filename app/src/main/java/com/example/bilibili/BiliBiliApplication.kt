package com.example.bilibili

import android.app.Application
import com.example.bilibili.util.SPUtils
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

class BiliBiliApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SPUtils.init(this)
        // m3u8 使用 ExoPlayer（Media3），HLS seek/时长比默认 IJK 更稳
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
    }
}

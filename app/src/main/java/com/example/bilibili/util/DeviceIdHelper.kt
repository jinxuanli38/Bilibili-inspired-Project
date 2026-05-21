package com.example.bilibili.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceIdHelper {

    private const val KEY_DEVICE_ID = "app_device_id"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.trim().orEmpty()
        if (androidId.isNotEmpty() && androidId != "9774d56d682e549c") {
            return androidId
        }
        var cached = SPUtils.getDeviceId()
        if (cached.isEmpty()) {
            cached = UUID.randomUUID().toString()
            SPUtils.saveDeviceId(cached)
        }
        return cached
    }
}

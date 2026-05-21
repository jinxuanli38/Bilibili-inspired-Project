package com.example.bilibili.util

import android.app.Activity
import android.content.Intent
import com.example.bilibili.MainActivity
import org.json.JSONObject

object AuthSessionHelper {

    fun saveLoginData(data: JSONObject) {
        SPUtils.saveToken(data.getString("token"))
        SPUtils.saveCurrentCoinCount(data.optInt("currentCoinCount"))
        SPUtils.saveUserId(data.getString("userId"))
        SPUtils.saveAvatar(data.optString("avatar"))
        SPUtils.saveNickname(data.getString("nickName"))
    }

    fun navigateToMainAndFinish(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finishAffinity()
    }
}

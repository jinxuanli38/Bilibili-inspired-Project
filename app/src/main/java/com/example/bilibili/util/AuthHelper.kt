package com.example.bilibili.util

import com.example.bilibili.data.api.AccountService
import org.json.JSONObject

/**
 * 启动时根据本地 token 决定进入主页或登录页。
 */
object AuthHelper {

    sealed class LaunchTarget {
        data object Main : LaunchTarget()
        data object Login : LaunchTarget()
    }

    suspend fun resolveLaunchTarget(): LaunchTarget {
        val token = SPUtils.getToken()
        if (token.isEmpty()) {
            return LaunchTarget.Login
        }
        return try {
            val accountService = RetrofitClient.create(AccountService::class.java)
            val response = JSONObject(accountService.autoLogin(token))
            if (response.optInt("code") != 200) {
                SPUtils.cleanToken()
                return LaunchTarget.Login
            }
            val data = response.optJSONObject("data") ?: return LaunchTarget.Login.also { SPUtils.cleanToken() }
            val resToken = data.optString("token", "")
            if (resToken.isEmpty()) {
                SPUtils.cleanToken()
                LaunchTarget.Login
            } else {
                if (resToken != token) {
                    SPUtils.saveToken(resToken)
                }
                LaunchTarget.Main
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SPUtils.cleanToken()
            LaunchTarget.Login
        }
    }
}

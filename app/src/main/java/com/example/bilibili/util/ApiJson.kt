package com.example.bilibili.util

import org.json.JSONObject

object ApiJson {
    fun JSONObject.isSuccess(): Boolean =
        optInt("code") == 200 || optString("status") == "success"

    fun JSONObject.errorMessage(default: String = "请求失败"): String =
        optString("message").ifEmpty { optString("info", default) }

    fun JSONObject.dataObject(): JSONObject? = optJSONObject("data")
}

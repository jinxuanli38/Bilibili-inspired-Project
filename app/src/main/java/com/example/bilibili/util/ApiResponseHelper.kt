package com.example.bilibili.util

import org.json.JSONObject

object ApiResponseHelper {

    fun isSuccess(response: String): Boolean {
        return try {
            val json = JSONObject(response)
            json.optString("status") == "success" || json.optInt("code") == 200
        } catch (_: Exception) {
            false
        }
    }

    fun errorMessage(response: String, fallback: String = "请求失败"): String {
        return try {
            JSONObject(response).optString("message").ifBlank { fallback }
        } catch (_: Exception) {
            fallback
        }
    }

    fun successData(response: String): String {
        return try {
            val json = JSONObject(response)
            when (val data = json.opt("data")) {
                null, JSONObject.NULL -> ""
                is String -> data
                else -> data.toString().trim('"')
            }
        } catch (_: Exception) {
            ""
        }
    }
}

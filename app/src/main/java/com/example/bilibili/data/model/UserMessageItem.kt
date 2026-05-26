package com.example.bilibili.data.model

import org.json.JSONObject

data class UserMessageItem(
    val messageId: Int,
    val messageType: Int,
    val sendUserId: String,
    val sendUserName: String,
    val sendUserAvatar: String,
    val videoId: String,
    val videoName: String,
    val videoCover: String,
    val messageContent: String,
    val messageContentReply: String,
    val createTimeRaw: String,
) {
    companion object {
        fun fromJson(json: JSONObject): UserMessageItem {
            val extend = json.optJSONObject("extendDto")
                ?: run {
                    val extendJson = json.optString("extendJson", "")
                    if (extendJson.isEmpty()) JSONObject() else JSONObject(extendJson)
                }
            return UserMessageItem(
                messageId = json.optInt("messageId"),
                messageType = json.optInt("messageType"),
                sendUserId = json.optString("sendUserId", ""),
                sendUserName = json.optString("sendUserName", ""),
                sendUserAvatar = json.optString("sendUserAvatar", ""),
                videoId = json.optString("videoId", ""),
                videoName = json.optString("videoName", ""),
                videoCover = json.optString("videoCover", ""),
                messageContent = extend.optString("messageContent", ""),
                messageContentReply = extend.optString("messageContentReply", ""),
                createTimeRaw = json.optString("createTime", ""),
            )
        }
    }
}

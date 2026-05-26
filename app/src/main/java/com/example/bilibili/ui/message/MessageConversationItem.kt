package com.example.bilibili.ui.message

import androidx.annotation.DrawableRes

data class MessageConversationItem(
    val name: String,
    val preview: String,
    val time: String,
    @DrawableRes val avatarRes: Int? = null,
    @DrawableRes val avatarBackgroundRes: Int? = null,
    @DrawableRes val avatarIconRes: Int? = null,
    val highlightName: Boolean = false,
    val unreadStyle: MessageUnreadStyle = MessageUnreadStyle.NONE,
    val unreadCount: Int = 0,
)

enum class MessageUnreadStyle {
    NONE,
    DOT,
    COUNT,
}

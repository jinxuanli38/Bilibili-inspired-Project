package com.example.bilibili.ui.message

import com.example.bilibili.data.model.UserMessageItem

/** 同一用户多条「关注了我」只保留最新一条（列表按 message_id 降序）。 */
object FanMessageDedupe {

    fun dedupe(items: List<UserMessageItem>): List<UserMessageItem> {
        val seenSenders = mutableSetOf<String>()
        return items.filter { item ->
            if (item.messageType != MessageTypes.FANS) {
                true
            } else {
                val sender = item.sendUserId
                if (sender.isEmpty()) true else seenSenders.add(sender)
            }
        }
    }
}

package com.easylive.service;

import com.easylive.entity.po.UserMessage;

/**
 * 新消息推送抽象，由 easylive-web 中的 SSE 实现。
 */
public interface UserMessagePushService {

    void onNewMessage(String userId, UserMessage message);

    void onUnreadChanged(String userId);

    /** 被关注用户的粉丝数变化（关注/取关后推送给该用户） */
    void onFansCountChanged(String userId, int fansCount);

    /** 关注者的关注数变化（关注/取关后推送给操作者） */
    void onFocusCountChanged(String userId, int focusCount);
}

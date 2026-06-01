# 关注/粉丝数实时推送（SSE）

## 后端（已写入 `~/Downloads/easylive-main/easylive/`）

1. `UserMessagePushService.java` — 已加 `onFansCountChanged` / `onFocusCountChanged`
2. `MessageSseHub.java` — 已发 `fans_count` / `focus_count` 事件
3. `UserFocusServiceImpl.java` — 关注/取关后推送

**重启 easylive-web（7071）后生效。**

SSE JSON 示例：
   - `{"event":"fans_count","userId":"xxx","fansCount":123}`
   - `{"event":"focus_count","userId":"xxx","focusCount":45}`

## 客户端（已实现）

- `FollowStatsCenter`：乐观 ±1 + 接收 SSE 绝对值
- `FocusActionHelper`：统一关注接口，成功后 `publishLocalChange`
- `RealtimeSseClient`：Application 与消息页共用 SSE
- 个人页、他人主页、播放页 UP 主粉丝数联动

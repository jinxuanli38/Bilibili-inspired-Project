# 消息：新增粉丝 + loadMessage 扩展

## 后端改动

1. `MessageTypeEnum` 增加 `FANS(5, "新增粉丝")`
2. `UserMessageService.saveFollowMessage` — 关注时写入 `user_message`
3. `UserFocusServiceImpl.focus()` 调用 `saveFollowMessage`
4. `loadMessage` 增加参数：
   - `messageTypes`：如 `"2,3"` 同时查点赞与收藏
   - `atMeOnly`：`1` 时筛 @我（extend 含 `@昵称`）
   - **评论 type=4 时自动 `replyOnly=1`**：只返回「回复了我的评论」（`messageContentReply` 非空），不含视频下直接评论

## 接口示例

```
POST /message/loadMessage
messageType=4&pageNo=1                    // 回复与@ - 全部
messageType=4&pageNo=1&atMeOnly=1         // 回复与@ - @我
messageType=2&pageNo=1&messageTypes=2,3   // 收到喜欢（点赞+收藏）
messageType=5&pageNo=1                    // 新增粉丝
```

返回 `data.list[]` 字段：`sendUserName`, `sendUserAvatar`, `videoName`, `videoCover`, `extendDto.messageContent`, `extendDto.messageContentReply`, `createTime`, `messageType`

重启 **easylive-web** 后生效。

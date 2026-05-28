# 消息收件箱 loadAllMessage

## 接口

```
POST /message/loadAllMessage
pageNo=1
```

返回点赞(2)、收藏(3)、回复(4，仅回复评论)、粉丝(5)，按 `message_id desc` 分页。

## 后端改动

- `UserMessageController.loadAllMessage`
- `UserMessageQuery.inboxFeed=1`：评论类型仅保留含 `messageContentReply` 的记录，其它类型不受影响
- `UserMessageMapper.xml` 增加 `inboxFeed` 条件

重启 **easylive-web** 后生效。

## Android

- `MessageFragment` 列表调用 `loadAllMessage`，会话样式展示混合消息
- 「收到喜欢」取消客户端聚合，一条消息一行

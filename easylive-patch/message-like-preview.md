# 收到喜欢：评论文案 / 视频封面

## 后端改动（已写入 `easylive` 源码，需重启 easylive-web）

### 1. `UserMessageExtendDto`
- `commentId`：评论点赞为评论 ID，视频点赞/收藏为 `0`
- `previewType`：`comment` | `video`

### 2. `MessageOperationAspect`
- 读取 `commentId` 参数（`doAction` 接口）
- 调用 `saveMessage(..., actionType)`

### 3. `UserMessageServiceImpl.saveMessage`
- **评论点赞**：接收人为评论作者，`messageContentReply` = 评论正文，`previewType=comment`
- **视频点赞/收藏**：`messageContent` = 视频标题，`previewType=video`；列表 JOIN 已有 `videoCover`
- **去重**：同一发送人对同一目标只记一条（按 `sendUserId` + `commentId`/`video`）
- **取消点赞**不再写消息：仅当 `user_action` 仍存在时插入

### 接口返回（无需新字段）
`extendDto.commentId`、`extendDto.previewType`、`extendDto.messageContentReply`、`videoCover`

## Android
- 评论目标：右侧 `tv_preview` 显示评论
- 视频目标：右侧 `iv_preview_cover` 显示封面
- 文案区分「赞了我的评论 / 赞了我的视频 / 收藏了我的视频」

## 部署
```bash
# 重新编译并重启 easylive-web
```

旧数据无 `previewType` 时，客户端用 `messageContentReply` 是否为空推断。

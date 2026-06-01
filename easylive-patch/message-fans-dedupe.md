# 新增粉丝消息去重 + 取关隐藏

## 关注

同一 `sendUserId` 对同一 `receiveUserId` 再次关注时：更新已有 type=5 记录，删重复行。

## 取关

`cancelFocus` → `removeFollowMessage` 删除对应粉丝消息，并 `onUnreadChanged`。

## 列表查询

`hideInactiveFans=1`：type=5 仅当 `user_focus` 仍存在（对方仍关注我）时返回。用于 `loadMessage`(type=5)、`loadAllMessage`、`findNoReadCountGroup`。

## App

`FanMessageDedupe`：按 sendUserId 去重（兼容旧数据）。

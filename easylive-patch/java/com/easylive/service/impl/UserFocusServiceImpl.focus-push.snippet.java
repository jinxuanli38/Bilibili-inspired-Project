// 在 UserFocusServiceImpl 中注入 UserMessagePushService，并在关注/取关成功后推送粉丝数、关注数。
// 粉丝数：COUNT(*) FROM user_focus WHERE focus_user_id = 被关注者
// 关注数：COUNT(*) FROM user_focus WHERE user_id = 操作者
//
// 示例（关注成功后）：
//
// @Resource
// private UserMessagePushService userMessagePushService;
//
// public void focus(String focusUserId) {
//     // ... 原有插入关注记录 ...
//     String operatorUserId = getCurrentUserId();
//     int targetFans = userFocusMapper.countByFocusUserId(focusUserId);
//     int operatorFocus = userFocusMapper.countByUserId(operatorUserId);
//     userMessagePushService.onFansCountChanged(focusUserId, targetFans);
//     userMessagePushService.onFocusCountChanged(operatorUserId, operatorFocus);
// }
//
// public void cancelFocus(String focusUserId) {
//     // ... 原有删除关注记录 ...
//     // 同上推送最新 count
// }

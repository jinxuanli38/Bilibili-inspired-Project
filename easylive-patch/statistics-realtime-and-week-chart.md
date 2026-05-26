# 统计数据来源与实时化改造说明

## 一、数据是不是都在 `statistics_info`？

**不完全是。**

| 展示位置 | 接口 | 数据来源 |
|---------|------|----------|
| 顶部大数字（总播放、总评论…） | `getActualTimeStatisticsInfo` → `totalCountInfo` | **`video_info` 表 SUM**（实时累计） |
| 顶部「粉丝」 | 同上 | **`user_focus` 实时 COUNT** |
| 卡片「昨日新增」 | 同上 → `preDayData` | **`statistics_info` 里「昨天」那一行** |
| **折线图近 7 天** | `getWeekStatisticsInfo` | **`statistics_info` 按天、按类型** |

所以：**折线图和「昨日新增」依赖 `statistics_info`；顶部总量多数是实时表汇总，不是这张表。**

---

## 二、定时任务是不是每天 0 点？

是的。`SysTask`：

```java
@Scheduled(cron = "0 0 10 * * ?")  // 每天 10:00
public void updateStatisticsInfo() {
    statisticsInfoService.updateStatisticsInfo();
}
```

逻辑要点：

- 只处理 **昨天**（`getBeforeDayDate(1)`）
- 从 Redis / 业务表 **汇总昨天一整天的增量**，再 `insertOrUpdateBatch` 写入 `statistics_info`
- **不会**在白天随用户操作更新

原折线图日期 `getBeforeDates(7)`：**过去 7 天且不含今天**（昨天往前数 7 天）。

---

## 三、已做的改造（需重启 easylive-web）

### 1. 近 7 天含今天

- 新增 `DateUtils.getRecentDatesIncludingToday(7)`
- `StatisticsController.getWeekStatisticsInfo` 改为使用该方法  
- 横轴：6 天前 → … → **今天**

### 2. 交互后实时写入 `statistics_info`（今天）

- 新增 `incrementStatisticsCount` SQL：`ON DUPLICATE KEY UPDATE statistics_count = statistics_count + delta`
- 新增 `StatisticsInfoService.incrementStatistics(userId, dataType, delta)`，`statistics_data = 今天`

挂钩位置：

| 交互 | 类 | dataType |
|------|-----|----------|
| 播放 +1 | `VideoInfoServiceImpl.addReadCount` | PLAY(0) |
| 关注 / 取关 | `UserFocusServiceImpl` | FANS(1) ±1 |
| 点赞/取消、收藏/取消 | `UserActionServiceImpl` | LIKE(2) / COLLECTION(3) |
| 投币 | `UserActionServiceImpl` | COIN(4) |
| 一级评论 / 删除 | `VideoCommentServiceImpl` | COMMENT(5) |
| 弹幕 / 删除 | `VideoDanmuServiceImpl` | DANMU(6) |

效果：用户操作后，再打开统计页或下拉刷新，**今天**对应折线点会立刻变化。

### 3. 定时任务仍保留

每天 0 点仍会 **校正昨天** 的数据（与 Redis/业务表对齐）。  
**今天** 靠实时累加；**昨天及更早** 靠定时任务 + 历史行。

---

## 四、使用与验证

1. **重启** `easylive-web`（及 admin 若用管理端图表）
2. 登录 UP 主账号，对自己视频：播放、评论、点赞等
3. App 统计页下拉刷新，切到对应指标，看 **今天** 是否上涨
4. 数据库检查：

```sql
SELECT * FROM statistics_info
WHERE user_id = '你的userId'
  AND statistics_data = CURDATE()
ORDER BY data_type;
```

---

## 五、可选后续

- 卡片「昨日新增」旁增加「今日新增」：读今天 `statistics_info` 行
- 管理端 `IndexController.getWeekStatisticsInfo` 同样改为 `getRecentDatesIncludingToday(7)`
- 历史数据为 0：可对今天手动 `increment` 或等真实交互产生

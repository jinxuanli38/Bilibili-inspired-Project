# 近 7 天 statistics_info 数据回填

从现有业务表（评论、弹幕、关注、点赞/收藏/投币、播放历史）汇总写入 `statistics_info`，与后端定时任务 `updateStatisticsInfo()` 的按日统计口径一致，时间范围为 **今天起往前共 7 天（含今天）**。

## 执行方式

```bash
mysql -h127.0.0.1 -uroot -p easylive < easylive-patch/backfill_statistics_info_7days.sql
```

或在 MySQL 客户端中：

```sql
USE easylive;
SOURCE /path/to/bilibili/easylive-patch/backfill_statistics_info_7days.sql;
```

数据库账号密码与 `easylive-web` 的 `application.yml` 一致（默认库名 `easylive`）。

## 说明

| data_type | 含义 | 数据来源 |
|-----------|------|----------|
| 0 | 播放 | `video_play_history` + `video_info`（按当日 `last_update_time` 计数，为近似值） |
| 1 | 粉丝 | `user_focus` |
| 2 | 点赞 | `user_action` type=2 |
| 3 | 收藏 | `user_action` type=3 |
| 4 | 投币 | `user_action` type=4，`SUM(action_count)` |
| 5 | 评论 | `video_comment`（含回复） |
| 6 | 弹幕 | `video_danmu` + `video_info` |

- 使用 `INSERT ... ON DUPLICATE KEY UPDATE`，主键 `(statistics_data, user_id, data_type)` 已存在则覆盖为重新统计的值。
- 若需 **完全重算** 这 7 天，可取消 SQL 文件开头 `DELETE` 注释后再执行。
- **播放**：正式环境定时任务读 Redis 日播放量；无 Redis 历史时用播放历史表近似，可能低于真实播放。
- 执行后 **重启或刷新 App 数据统计页** 即可看到折线图变化（需后端周统计接口日期范围含今天）。

## 查看某个 UP 主

```sql
SELECT * FROM statistics_info
WHERE user_id = '你的userId'
  AND statistics_data >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '%Y-%m-%d')
ORDER BY statistics_data, data_type;
```

-- =============================================================================
-- 从业务表回填 statistics_info：近 7 天（含今天）
-- 逻辑与 StatisticsInfoServiceImpl.updateStatisticsInfo / Mapper 按日统计一致
--
-- data_type: 0播放 1粉丝 2点赞 3收藏 4投币 5评论 6弹幕
-- 执行前请 USE easylive;  建议先备份 statistics_info
-- =============================================================================

SET @range_start := DATE_SUB(CURDATE(), INTERVAL 6 DAY);
SET @range_end_exclusive := DATE_ADD(CURDATE(), INTERVAL 1 DAY);

-- 可选：先清空这 7 天内已有统计，再全量重算（取消下面注释即可）
-- DELETE FROM statistics_info
-- WHERE STR_TO_DATE(statistics_data, '%Y-%m-%d') >= @range_start
--   AND STR_TO_DATE(statistics_data, '%Y-%m-%d') < @range_end_exclusive;

-- -----------------------------------------------------------------------------
-- 0 播放（近似：按 video_play_history 当日更新次数汇总到 UP 主）
-- 定时任务用 Redis 日键；无 Redis 历史时以此为准，可能低于真实播放量
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(h.last_update_time, '%Y-%m-%d') AS statistics_data,
       v.user_id,
       0 AS data_type,
       COUNT(1) AS statistics_count
FROM video_play_history h
         INNER JOIN video_info v ON h.video_id = v.video_id
WHERE h.last_update_time >= @range_start
  AND h.last_update_time < @range_end_exclusive
  AND v.user_id IS NOT NULL
  AND v.user_id != ''
GROUP BY DATE_FORMAT(h.last_update_time, '%Y-%m-%d'), v.user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 1 粉丝（user_focus.focus_user_id = 被关注 UP 主）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(uf.focus_time, '%Y-%m-%d') AS statistics_data,
       uf.focus_user_id AS user_id,
       1 AS data_type,
       COUNT(1) AS statistics_count
FROM user_focus uf
WHERE uf.focus_time >= @range_start
  AND uf.focus_time < @range_end_exclusive
  AND uf.focus_user_id IS NOT NULL
  AND uf.focus_user_id != ''
GROUP BY DATE_FORMAT(uf.focus_time, '%Y-%m-%d'), uf.focus_user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 2 点赞（user_action.action_type = 2）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(ua.action_time, '%Y-%m-%d') AS statistics_data,
       ua.video_user_id AS user_id,
       2 AS data_type,
       COUNT(1) AS statistics_count
FROM user_action ua
WHERE ua.action_type = 2
  AND ua.action_time >= @range_start
  AND ua.action_time < @range_end_exclusive
  AND ua.video_user_id IS NOT NULL
  AND ua.video_user_id != ''
GROUP BY DATE_FORMAT(ua.action_time, '%Y-%m-%d'), ua.video_user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 3 收藏（user_action.action_type = 3）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(ua.action_time, '%Y-%m-%d') AS statistics_data,
       ua.video_user_id AS user_id,
       3 AS data_type,
       COUNT(1) AS statistics_count
FROM user_action ua
WHERE ua.action_type = 3
  AND ua.action_time >= @range_start
  AND ua.action_time < @range_end_exclusive
  AND ua.video_user_id IS NOT NULL
  AND ua.video_user_id != ''
GROUP BY DATE_FORMAT(ua.action_time, '%Y-%m-%d'), ua.video_user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 4 投币（action_type = 4，按枚数 SUM(action_count)，与定时任务乘 action_count 等价）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(ua.action_time, '%Y-%m-%d') AS statistics_data,
       ua.video_user_id AS user_id,
       4 AS data_type,
       IFNULL(SUM(IFNULL(ua.action_count, 1)), 0) AS statistics_count
FROM user_action ua
WHERE ua.action_type = 4
  AND ua.action_time >= @range_start
  AND ua.action_time < @range_end_exclusive
  AND ua.video_user_id IS NOT NULL
  AND ua.video_user_id != ''
GROUP BY DATE_FORMAT(ua.action_time, '%Y-%m-%d'), ua.video_user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 5 评论（含回复，与 selectCommentsCountByDate 一致）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(vc.post_time, '%Y-%m-%d') AS statistics_data,
       vc.video_user_id AS user_id,
       5 AS data_type,
       COUNT(1) AS statistics_count
FROM video_comment vc
WHERE vc.post_time >= @range_start
  AND vc.post_time < @range_end_exclusive
  AND vc.video_user_id IS NOT NULL
  AND vc.video_user_id != ''
GROUP BY DATE_FORMAT(vc.post_time, '%Y-%m-%d'), vc.video_user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 6 弹幕（与 selectDanmuCountByDate 一致：按视频 UP 主汇总）
-- -----------------------------------------------------------------------------
INSERT INTO statistics_info (statistics_data, user_id, data_type, statistics_count)
SELECT DATE_FORMAT(d.post_time, '%Y-%m-%d') AS statistics_data,
       v.user_id,
       6 AS data_type,
       COUNT(1) AS statistics_count
FROM video_danmu d
         INNER JOIN video_info v ON d.video_id = v.video_id
WHERE d.post_time >= @range_start
  AND d.post_time < @range_end_exclusive
  AND v.user_id IS NOT NULL
  AND v.user_id != ''
GROUP BY DATE_FORMAT(d.post_time, '%Y-%m-%d'), v.user_id
ON DUPLICATE KEY UPDATE statistics_count = VALUES(statistics_count);

-- -----------------------------------------------------------------------------
-- 校验：近 7 天各类型行数（可按 user_id 再筛）
-- -----------------------------------------------------------------------------
SELECT statistics_data,
       data_type,
       SUM(statistics_count) AS total_count,
       COUNT(DISTINCT user_id) AS up_count
FROM statistics_info
WHERE STR_TO_DATE(statistics_data, '%Y-%m-%d') >= @range_start
  AND STR_TO_DATE(statistics_data, '%Y-%m-%d') < @range_end_exclusive
GROUP BY statistics_data, data_type
ORDER BY statistics_data, data_type;

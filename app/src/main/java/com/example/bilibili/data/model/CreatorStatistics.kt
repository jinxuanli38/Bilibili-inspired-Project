package com.example.bilibili.data.model

data class CreatorStatItem(
    val label: String,
    val total: Int,
    val yesterdayDelta: Int,
    val dataType: Int,
)

data class ChartPoint(
    val dateLabel: String,
    val value: Int,
)

data class CreatorChart(
    val dataType: Int,
    val title: String,
    val points: List<ChartPoint>,
    /** 气泡内指标名，如「播放量」 */
    val metricLabel: String = "",
    val loadFailed: Boolean = false,
)

data class CreatorServiceEntry(
    val title: String,
    val action: ServiceAction,
)

enum class ServiceAction {
    VIDEO_POST,
    COMMENT,
    DANMU,
}

data class CreatorStatistics(
    val nickname: String,
    val avatar: String,
    val stats: List<CreatorStatItem>,
    val services: List<CreatorServiceEntry>,
)

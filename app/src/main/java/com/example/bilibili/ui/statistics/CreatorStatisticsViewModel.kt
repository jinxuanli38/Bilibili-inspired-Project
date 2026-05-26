package com.example.bilibili.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.api.StatisticsService
import com.example.bilibili.data.model.ChartPoint
import com.example.bilibili.data.model.CreatorChart
import com.example.bilibili.data.model.CreatorServiceEntry
import com.example.bilibili.data.model.CreatorStatItem
import com.example.bilibili.data.model.CreatorStatistics
import com.example.bilibili.data.model.ServiceAction
import com.example.bilibili.util.ApiJson.errorMessage
import com.example.bilibili.util.ApiJson.isSuccess
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.SPUtils
import com.example.bilibili.util.optNormalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreatorStatisticsViewModel : ViewModel() {

    private val statisticsService = RetrofitClient.create(StatisticsService::class.java)
    private val postService = RetrofitClient.create(PostService::class.java)

    private val _uiState = MutableLiveData<UiState>(UiState.Loading)
    val uiState: LiveData<UiState> = _uiState

    private val _chart = MutableLiveData<CreatorChart>()
    val chart: LiveData<CreatorChart> = _chart

    private val _selectedDataType = MutableLiveData(1)
    val selectedDataType: LiveData<Int> = _selectedDataType

    private var cachedStats: CreatorStatistics? = null

    fun refresh() {
        if (SPUtils.getToken().isEmpty()) {
            _uiState.value = UiState.NeedLogin
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val stats = withContext(Dispatchers.IO) { loadStatistics() }
                cachedStats = stats
                _uiState.value = UiState.Success(stats)
                loadChart(stats.stats.firstOrNull()?.dataType ?: 1)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadChart(dataType: Int) {
        val stats = cachedStats ?: return
        _selectedDataType.value = dataType
        val label = stats.stats.firstOrNull { it.dataType == dataType }?.label ?: "数据"
        viewModelScope.launch {
            try {
                val chart = withContext(Dispatchers.IO) {
                    loadWeekChart(dataType, label)
                }
                _chart.value = chart
            } catch (e: Exception) {
                e.printStackTrace()
                _chart.value = CreatorChart(
                    dataType = dataType,
                    title = "近7天$label",
                    points = buildFallbackWeekPoints(),
                    metricLabel = chartMetricLabel(label),
                    loadFailed = true,
                )
            }
        }
    }

    private suspend fun loadStatistics(): CreatorStatistics {
        val statsResponse = JSONObject(statisticsService.getActualTimeStatisticsInfo())
        if (!statsResponse.isSuccess()) {
            throw IllegalStateException(statsResponse.errorMessage("加载统计数据失败"))
        }

        val data = statsResponse.getJSONObject("data")
        val total = data.getJSONObject("totalCountInfo")
        val preDay = data.optJSONObject("preDayData") ?: JSONObject()

        val userId = SPUtils.getUserId()
        var nickname = SPUtils.getNickname()
        var avatar = SPUtils.getAvatar().orEmpty()
        if (userId.isNotEmpty()) {
            val userResponse = JSONObject(postService.getUserInfo(userId))
            if (userResponse.isSuccess()) {
                val userData = userResponse.getJSONObject("data")
                nickname = userData.optNormalizedString("nickName").ifEmpty { nickname.ifEmpty { "用户" } }
                avatar = userData.optString("avatar", avatar)
            }
        }

        val stats = listOf(
            statItem("总粉丝", total.optInt("fansCount"), preDay, 1),
            statItem("总播放", total.optInt("playCount"), preDay, 0),
            statItem("总评论", total.optInt("commentCount"), preDay, 5),
            statItem("总弹幕", total.optInt("danmuCount"), preDay, 6),
            statItem("总点赞", total.optInt("likeCount"), preDay, 2),
            statItem("总收藏", total.optInt("collectCount"), preDay, 3),
            statItem("总投币", total.optInt("coinCount"), preDay, 4),
        )

        return CreatorStatistics(
            nickname = nickname.ifEmpty { "用户" },
            avatar = avatar,
            stats = stats,
            services = listOf(
                CreatorServiceEntry("稿件管理", ServiceAction.VIDEO_POST),
                CreatorServiceEntry("评论管理", ServiceAction.COMMENT),
                CreatorServiceEntry("弹幕管理", ServiceAction.DANMU),
            ),
        )
    }

    private suspend fun loadWeekChart(dataType: Int, metricLabel: String): CreatorChart {
        val response = JSONObject(statisticsService.getWeekStatisticsInfo(dataType))
        if (!response.isSuccess()) {
            throw IllegalStateException(response.errorMessage("加载图表失败"))
        }
        val points = parseWeekChartPoints(response.optJSONArray("data"))
        if (points.isEmpty()) {
            throw IllegalStateException("图表数据为空")
        }
        return CreatorChart(
            dataType = dataType,
            title = "近7天$metricLabel",
            points = points,
            metricLabel = chartMetricLabel(metricLabel),
        )
    }

    /** 统计卡片「总播放」→ 气泡「播放量」 */
    private fun chartMetricLabel(statLabel: String): String = when {
        statLabel.contains("播放") -> "播放量"
        statLabel.contains("粉丝") -> "粉丝"
        statLabel.contains("评论") -> "评论"
        statLabel.contains("弹幕") -> "弹幕"
        statLabel.contains("点赞") -> "点赞"
        statLabel.contains("收藏") -> "收藏"
        statLabel.contains("投币") -> "投币"
        else -> statLabel.removePrefix("总")
    }

    private fun parseWeekChartPoints(array: JSONArray?): List<ChartPoint> {
        if (array == null || array.length() == 0) return emptyList()
        val points = mutableListOf<ChartPoint>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val date = item.optString("statisticsData", "")
            if (date.isEmpty()) continue
            val shortDate = if (date.length >= 10) date.substring(5) else date
            val count = when {
                !item.isNull("statisticsCount") -> item.optInt("statisticsCount")
                else -> 0
            }
            points.add(ChartPoint(dateLabel = shortDate, value = count))
        }
        return points
    }

    /** 接口失败时仍展示 7 天横轴，值为 0 */
    private fun buildFallbackWeekPoints(): List<ChartPoint> {
        val labelFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
        return (0 until 7).map {
            ChartPoint(dateLabel = labelFormat.format(calendar.time), value = 0).also {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun statItem(
        label: String,
        total: Int,
        preDay: JSONObject,
        dataType: Int,
    ): CreatorStatItem {
        val yesterday = preDay.optInt(dataType.toString(), 0)
        return CreatorStatItem(label, total, yesterday, dataType)
    }

    sealed class UiState {
        data object Loading : UiState()
        data object NeedLogin : UiState()
        data class Success(val data: CreatorStatistics) : UiState()
        data class Error(val message: String) : UiState()
    }
}

package com.example.bilibili.ui.personal.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(private val apiService: PostService) : ViewModel() {

    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList

    /**
     * 一次性获取视频列表
     */
    fun fetchHomeVideos(userId: String) {
        viewModelScope.launch {
            try {
                // 1. 调用接口（注意：这里的接口返回 String，需要解析）
                val responseString = apiService.loadVideoList(userId = userId, type = 0)

                // 2. 解析 JSON 逻辑（把你原来写在 PagingSource 里的逻辑挪到这里）
                val list = parseVideoJson(responseString)

                // 3. 更新 UI
                _videoList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseVideoJson(json: String): List<VideoItem> {
        val videoList = mutableListOf<VideoItem>()
        val jsonObject = JSONObject(json)
        if (jsonObject.optString("status") == "success") {
            val dataObj = jsonObject.getJSONObject("data")
            val jsonArray = dataObj.getJSONArray("list")
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                videoList.add(VideoItem(
                    videoId = item.getString("videoId"),
                    videoName = item.getString("videoName"),
                    videoCover = item.getString("videoCover"),
                    playCount = item.optInt("playCount"),
                    commentCount = item.optInt("commentCount"),
                    danmuCount = item.optInt("danmuCount"),
                    duration = item.optInt("duration"),
                    createTime = item.getString("createTime"),
                    nickName = item.getString("nickName")
                ))
            }
        }
        return videoList
    }
}
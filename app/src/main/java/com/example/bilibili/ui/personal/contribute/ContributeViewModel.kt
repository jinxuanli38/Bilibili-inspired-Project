package com.example.bilibili.ui.personal.contribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ContributeViewModel(private val apiService: PostService) : ViewModel() {
    val videoList = MutableLiveData<List<VideoItem>>()
    val errorMsg = MutableLiveData<String>()

    fun loadContributeVideos(userId: String, orderType: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responseString = apiService.loadVideoList(userId = userId, orderType = orderType)
                val jsonObject = JSONObject(responseString)

                if (jsonObject.optString("status") == "success") {
                    val listData = mutableListOf<VideoItem>()
                    val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        listData.add(VideoItem(
                            videoId = item.getString("videoId"),
                            videoName = item.getString("videoName"),
                            videoCover = item.getString("videoCover"),
                            playCount = item.optInt("playCount"),
                            commentCount = item.optInt("commentCount"),
                            duration = item.optInt("duration"),
                            createTime = item.getString("createTime"),
                            danmuCount = item.optInt("danmuCount"),
                            // 修正：JSON返回中nickName可能为null，做个空处理
                            nickName = if (item.isNull("nickName")) "未知UP" else item.getString("nickName")
                        ))
                    }
                    videoList.postValue(listData)
                } else {
                    errorMsg.postValue(jsonObject.optString("message"))
                }
            } catch (e: Exception) {
                errorMsg.postValue("网络错误: ${e.message}")
            }
        }
    }

    // 这个工厂类就像是一个“模具”，专门用来生产带参数的 ContributeViewModel
    class ContributeViewModelFactory(private val apiService: PostService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContributeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ContributeViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
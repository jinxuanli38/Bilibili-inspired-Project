package com.example.bilibili.ui.personal.collect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.model.CollectVideo
import com.example.bilibili.util.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class CollectViewModel : ViewModel() {
    val collectVideos = MutableLiveData<List<CollectVideo>>()

    fun loadCollections(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. 获取原始 String
                val postService = RetrofitClient.create(PostService::class.java)
                val responseString = postService.loadUserCollection(userId)

                // 2. 使用 org.json 解析
                val root = JSONObject(responseString)
                if (root.getString("status") == "success") {
                    val dataObj = root.getJSONObject("data")
                    val jsonArray = dataObj.getJSONArray("list")

                    val list = mutableListOf<CollectVideo>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        // 手动对应每一个字段
                        val video = CollectVideo(
                            actionId = item.getInt("actionId"),
                            videoId = item.getString("videoId"),
                            videoUserId = item.getString("videoUserId"),
                            commentId = item.getInt("commentId"),
                            actionType = item.getInt("actionType"),
                            actionCount = item.getInt("actionCount"),
                            userId = item.getString("userId"),
                            actionTime = item.getString("actionTime"),
                            videoCover = item.getString("videoCover"),
                            videoName = item.getString("videoName")
                        )
                        list.add(video)
                    }
                    collectVideos.postValue(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
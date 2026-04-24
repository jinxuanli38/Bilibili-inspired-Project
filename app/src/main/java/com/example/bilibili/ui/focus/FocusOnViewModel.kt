package com.example.bilibili.ui.focus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.util.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class FocusOnViewModel : ViewModel() {
    val friendList = MutableLiveData<List<UserFriend>>()
    private val service = RetrofitClient.create(PostService::class.java)

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 传 pageNo = 1
                val response = service.loadFocusList(1)
                val jsonObject = JSONObject(response)

                if (jsonObject.optString("status") == "success") {
                    val dataObj = jsonObject.getJSONObject("data")
                    val jsonArray = dataObj.getJSONArray("list")
                    val tempList = mutableListOf<UserFriend>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        // 使用 optString 替代 getString，防止字段缺失报错
                        tempList.add(UserFriend(
                            userId = item.optString("userId"),
                            otherUserId = item.optString("otherUserId"),
                            otherNickName = item.optString("otherNickName", "未知用户"),
                            otherAvatar = item.optString("otherAvatar"),
                            otherPersonalIntroduction = item.optString("otherPersonalIntroduction"),
                            focusType = item.optInt("focusType"),
                            focusTime = item.optString("focusTime")
                        ))
                    }
                    friendList.postValue(tempList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 取消关注
    fun cancelFollow(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 假设你的接口里有这个方法
                val response = service.cancelFocus(userId)
                if (JSONObject(response).optString("status") == "success") {
                    loadData() // 刷新列表
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
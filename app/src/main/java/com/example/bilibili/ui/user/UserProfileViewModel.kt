package com.example.bilibili.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.util.FocusActionHelper
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.SPUtils
import com.example.bilibili.util.UserInfoText
import com.example.bilibili.util.optNormalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UserInfo(
    val userId: String,
    val avatar: String,
    val nickName: String,
    val email: String,
    val sex: Int,
    val birthday: String,
    val school: String,
    val personalIntroduction: String,
    val noticeInfo: String,
    val theme: Int,
    val fansCount: Int,
    val focusCount: Int,
    val likeCount: Int,
    val playCount: Int,
    val haveFocus: Boolean,
    /** 0未关注 1已互粉 2已关注 */
    val focusType: Int = 0,
)

class UserProfileViewModel : ViewModel() {

    private val postService = RetrofitClient.create(PostService::class.java)

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _focusState = MutableLiveData<Boolean>()
    val focusState: LiveData<Boolean> = _focusState

    private val _focusType = MutableLiveData<Int>(0)
    val focusType: LiveData<Int> = _focusType

    fun setUserInfo(data: JSONObject) {
        _userInfo.value = UserInfo(
            userId = data.optString("userId"),
            avatar = data.optString("avatar"),
            nickName = data.optString("nickName"),
            email = data.optString("email"),
            sex = data.optInt("sex"),
            birthday = data.optNormalizedString("birthday"),
            school = data.optNormalizedString("school"),
            personalIntroduction = data.optNormalizedString("personalIntroduction"),
            noticeInfo = data.optNormalizedString("noticeInfo"),
            theme = data.optInt("theme"),
            fansCount = data.optInt("fansCount"),
            focusCount = data.optInt("focusCount"),
            likeCount = data.optInt("likeCount"),
            playCount = data.optInt("playCount"),
            haveFocus = data.optBoolean("haveFocus"),
            focusType = resolveFocusType(data),
        )

        _focusState.value = data.optBoolean("haveFocus")
        _focusType.value = resolveFocusType(data)
    }

    private fun resolveFocusType(data: JSONObject): Int {
        if (data.has("focusType")) {
            return data.optInt("focusType", 0)
        }
        return if (data.optBoolean("haveFocus")) 2 else 0
    }

    fun setFocused(focused: Boolean, type: Int = if (focused) 2 else 0) {
        _focusState.value = focused
        _focusType.value = type
    }

    fun applyFansCount(count: Int) {
        val info = _userInfo.value ?: return
        _userInfo.value = info.copy(fansCount = count.coerceAtLeast(0))
    }

    fun applyFocusCount(count: Int) {
        val info = _userInfo.value ?: return
        _userInfo.value = info.copy(focusCount = count.coerceAtLeast(0))
    }

    fun applyFollowChange(change: FollowStatsCenter.FollowChange) {
        var info = _userInfo.value ?: return
        if (info.userId == change.targetUserId) {
            val delta = if (change.followed) 1 else -1
            info = info.copy(fansCount = FollowStatsCenter.adjustFansCount(info.fansCount, delta))
        }
        val selfId = SPUtils.getUserId()
        if (info.userId == selfId) {
            FollowStatsCenter.focusDeltaForSelf(change)?.let { delta ->
                info = info.copy(focusCount = FollowStatsCenter.adjustFocusCount(info.focusCount, delta))
            }
        }
        _userInfo.value = info
    }

    fun toggleFocus(targetUserId: String) {
        viewModelScope.launch {
            val currentState = _focusState.value ?: false
            val currentType = _focusType.value ?: 0
            val newState = !currentState
            val newType = if (newState) 2 else 0
            val previousInfo = _userInfo.value

            _focusState.value = newState
            _focusType.value = newType
            syncUserInfoFocusState(newState, newType)

            val ok = FocusActionHelper.setFocus(postService, targetUserId, newState)
            if (!ok) {
                _focusState.value = currentState
                _focusType.value = currentType
                if (previousInfo != null) {
                    _userInfo.value = previousInfo
                }
            } else {
                refreshUserInfo(targetUserId)
            }
        }
    }

    private fun syncUserInfoFocusState(focused: Boolean, focusType: Int) {
        val info = _userInfo.value ?: return
        _userInfo.value = info.copy(haveFocus = focused, focusType = focusType)
    }

    fun refreshUserInfo(targetUserId: String) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    postService.getUserInfo(targetUserId)
                }
                val jsonObject = JSONObject(result)
                if (jsonObject.optInt("code") == 200) {
                    setUserInfo(jsonObject.getJSONObject("data"))
                }
            } catch (_: Exception) {
            }
        }
    }
}

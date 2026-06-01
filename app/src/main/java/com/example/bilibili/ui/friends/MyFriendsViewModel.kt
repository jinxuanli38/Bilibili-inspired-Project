package com.example.bilibili.ui.friends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 粉丝 / 关注两个 Tab 共用：关注关系变化后刷新列表，避免 ViewPager2 缓存导致状态不一致。
 */
class MyFriendsViewModel : ViewModel() {

    private val _refreshLists = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    val refreshLists: SharedFlow<Unit> = _refreshLists.asSharedFlow()

    fun notifyFollowRelationChanged() {
        _refreshLists.tryEmit(Unit)
    }
}

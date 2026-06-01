package com.example.bilibili.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.bilibili.data.api.PostService
import com.example.bilibili.util.FocusActionHelper
import com.example.bilibili.util.RetrofitClient
import kotlinx.coroutines.launch

class FocusOnViewModel : ViewModel() {
    private val service = RetrofitClient.create(PostService::class.java)

    // 使用Pager创建分页数据流
    val focusList = Pager(
        config = PagingConfig(
            pageSize = 20,           // 每页20条数据
            enablePlaceholders = false, // 禁用占位符
            initialLoadSize = 20     // 初始加载20条
        ),
        pagingSourceFactory = { FocusOnPagingSource() }
    ).flow.cachedIn(viewModelScope)

    suspend fun cancelFollow(userId: String): Boolean =
        FocusActionHelper.setFocus(service, userId, follow = false)
}
package com.example.bilibili.ui.front

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class CategoryVideoViewModel(private val categoryId: Int) : ViewModel() {

    // 分页数据流
    val videoList: Flow<androidx.paging.PagingData<com.example.bilibili.data.model.VideoItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { FrontPagePagingSource(categoryId) }
        ).flow.cachedIn(viewModelScope)
}
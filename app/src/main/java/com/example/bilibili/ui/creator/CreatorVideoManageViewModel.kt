package com.example.bilibili.ui.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bilibili.data.api.UcenterService
import com.example.bilibili.data.model.CreatorVideoPost
import com.example.bilibili.util.ApiJson.errorMessage
import com.example.bilibili.util.ApiJson.isSuccess
import com.example.bilibili.util.PagingDefaults
import com.example.bilibili.util.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreatorVideoManageViewModel : ViewModel() {

    private val service = RetrofitClient.create(UcenterService::class.java)
    private val statusFilter = MutableStateFlow<Int?>(null)

    val videos: Flow<PagingData<CreatorVideoPost>> = statusFilter.flatMapLatest { status ->
        Pager(
            config = PagingDefaults.videoListConfig(),
            pagingSourceFactory = { CreatorVideoPagingSource(status) },
        ).flow
    }.cachedIn(viewModelScope)

    fun setStatusFilter(status: Int?) {
        statusFilter.value = status
    }

    fun deleteVideo(videoId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = JSONObject(service.delVideo(videoId))
                if (response.isSuccess()) {
                    onResult(true, null)
                } else {
                    onResult(false, response.errorMessage())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}

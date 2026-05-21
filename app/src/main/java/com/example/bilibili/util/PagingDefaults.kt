package com.example.bilibili.util

import androidx.paging.PagingConfig
import org.json.JSONObject

object PagingDefaults {
    /** 与后端列表接口 pageSize 保持一致 */
    const val PAGE_SIZE = 15
    const val PREFETCH_DISTANCE = 4

    fun videoListConfig(): PagingConfig = PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = PAGE_SIZE,
        prefetchDistance = PREFETCH_DISTANCE,
        enablePlaceholders = false
    )

    /**
     * 根据后端 data 中的 pageNo / pageTotal 计算下一页。
     * 勿用 list.size >= loadSize：后端每页条数固定（如 10/15），小于 Paging 的 loadSize 时会误判为没有下一页。
     */
    fun nextPageKey(dataObject: JSONObject, currentPage: Int, listSize: Int = 0): Int? {
        val pageTotal = dataObject.optInt("pageTotal", -1)
        if (pageTotal > 0) {
            val pageNo = dataObject.optInt("pageNo", currentPage)
            return if (pageNo < pageTotal) pageNo + 1 else null
        }
        // 兼容未返回 pageTotal 的接口：本页满员则尝试加载下一页
        return if (listSize >= PAGE_SIZE) currentPage + 1 else null
    }
}

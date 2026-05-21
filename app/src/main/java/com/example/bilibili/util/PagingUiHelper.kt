package com.example.bilibili.util

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object PagingUiHelper {

    /**
     * 双列网格 + 分页 footer；footer（加载中 / 没有更多了）独占一整行
     */
    fun setupGridWithLoadStateFooter(
        recyclerView: RecyclerView,
        spanCount: Int,
        contentAdapter: PagingDataAdapter<*, *>,
        onRetry: () -> Unit
    ) {
        val footerAdapter = LoadStateAdapter(onRetry)
        val concatAdapter = contentAdapter.withLoadStateFooter(footerAdapter)
        val gridManager = GridLayoutManager(recyclerView.context, spanCount)
        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position >= contentAdapter.itemCount) spanCount else 1
            }
        }
        recyclerView.layoutManager = gridManager
        recyclerView.adapter = concatAdapter
    }

    /**
     * 单列列表 + 分页 footer
     */
    fun setupListWithLoadStateFooter(
        recyclerView: RecyclerView,
        contentAdapter: PagingDataAdapter<*, *>,
        onRetry: () -> Unit
    ) {
        val footerAdapter = LoadStateAdapter(onRetry)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = contentAdapter.withLoadStateFooter(footerAdapter)
    }

    /** 刷新完成后将列表滚回顶部，避免数据已重置但视口仍停在原位置 */
    fun scrollContentToTop(recyclerView: RecyclerView) {
        when (val lm = recyclerView.layoutManager) {
            is GridLayoutManager -> lm.scrollToPositionWithOffset(0, 0)
            is LinearLayoutManager -> lm.scrollToPositionWithOffset(0, 0)
            else -> recyclerView.scrollToPosition(0)
        }
    }
}

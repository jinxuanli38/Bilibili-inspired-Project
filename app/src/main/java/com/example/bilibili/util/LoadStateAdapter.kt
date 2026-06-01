package com.example.bilibili.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.databinding.ItemLoadStateBinding

class LoadStateAdapter(
    private val retry: () -> Unit,
    private val endMessage: String? = null,
    private val showEndOnlyWhenHasItems: Boolean = false,
    private val itemCountProvider: () -> Int = { 0 },
    private val refreshStateProvider: () -> LoadState = { LoadState.NotLoading(endOfPaginationReached = false) },
) : LoadStateAdapter<LoadStateAdapter.LoadStateViewHolder>() {

    /** 刷新前已展示「没有更多了」时，下拉刷新期间保持 footer 不变，避免闪烁 */
    private var showingEndFooter = false

    private fun isRefreshInProgress(): Boolean =
        refreshStateProvider() is LoadState.Loading

    private fun shouldHoldEndDuringRefresh(): Boolean =
        isRefreshInProgress() && showingEndFooter && itemCountProvider() > 0

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        val bindState = resolveBindState(loadState)
        holder.bind(bindState)
    }

    private fun resolveBindState(loadState: LoadState): LoadState {
        if (shouldHoldEndDuringRefresh()) {
            return LoadState.NotLoading(endOfPaginationReached = true)
        }
        when (loadState) {
            is LoadState.NotLoading -> {
                val showEnd = loadState.endOfPaginationReached &&
                    (!showEndOnlyWhenHasItems || itemCountProvider() > 0)
                showingEndFooter = showEnd
            }
            else -> {
                if (!isRefreshInProgress()) showingEndFooter = false
            }
        }
        return loadState
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        if (shouldHoldEndDuringRefresh()) return true

        return when (loadState) {
            is LoadState.Loading, is LoadState.Error -> true
            is LoadState.NotLoading -> {
                if (!loadState.endOfPaginationReached) {
                    if (!isRefreshInProgress()) showingEndFooter = false
                    return false
                }
                val show = !showEndOnlyWhenHasItems || itemCountProvider() > 0
                if (show) showingEndFooter = true
                show
            }
        }
    }

    inner class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        private val retry: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            when (loadState) {
                is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvLoadingHint.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvLoadingHint.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "加载失败，点击重试"
                    binding.tvError.setTextColor(0xFFFF4D4F.toInt())
                    binding.btnRetry.visibility = View.VISIBLE
                    binding.btnRetry.setOnClickListener { retry() }
                }
                is LoadState.NotLoading -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvLoadingHint.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = endMessage ?: "没有更多了"
                    binding.tvError.setTextColor(0xFF9499A0.toInt())
                }
            }
        }
    }
}

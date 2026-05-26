package com.example.bilibili.ui.creator

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.R
import com.example.bilibili.databinding.ActivityCreatorListBinding
import com.example.bilibili.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatorVideoManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorListBinding
    private val viewModel: CreatorVideoManageViewModel by viewModels()
    private val adapter = CreatorVideoPostAdapter { item ->
        confirmDelete(item.videoId, item.videoName)
    }

    private val tabStatus = listOf(
        null to R.string.creator_tab_all,
        3 to R.string.creator_tab_pass,
        -1 to R.string.creator_tab_process,
        4 to R.string.creator_tab_fail,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.creator_video_manage_title)
        binding.ivBack.setOnClickListener { finish() }
        binding.tabLayout.isVisible = true
        binding.spinnerVideo.isVisible = false

        setupTabs()
        setupList()
        observeVideos()
    }

    private fun setupTabs() {
        tabStatus.forEach { (_, titleRes) ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(titleRes))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setStatusFilter(tabStatus[tab.position].first)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun setupList() {
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter
        binding.swipeRefresh.setColorSchemeResources(R.color.bili_pink)
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
        adapter.addLoadStateListener { state ->
            binding.swipeRefresh.isRefreshing = state.refresh is LoadState.Loading
            binding.progress.isVisible = state.refresh is LoadState.Loading && adapter.itemCount == 0
            binding.tvEmpty.isVisible = state.refresh is LoadState.NotLoading &&
                adapter.itemCount == 0
        }
    }

    private fun observeVideos() {
        lifecycleScope.launch {
            viewModel.videos.collectLatest { adapter.submitData(it) }
        }
    }

    private fun confirmDelete(videoId: String, title: String) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.creator_delete_video_confirm) + "\n$title")
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.deleteVideo(videoId) { ok, msg ->
                    if (ok) {
                        ToastUtils.showShort(this, "已删除")
                        adapter.refresh()
                    } else {
                        ToastUtils.showShort(this, msg ?: "删除失败")
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

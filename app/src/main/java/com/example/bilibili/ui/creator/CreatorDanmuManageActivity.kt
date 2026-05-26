package com.example.bilibili.ui.creator

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatorDanmuManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorListBinding
    private val viewModel: CreatorInteractionManageViewModel by viewModels()
    private val adapter = CreatorDanmuAdapter { item ->
        confirmDelete(item.danmuId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.creator_danmu_manage_title)
        binding.ivBack.setOnClickListener { finish() }
        binding.tabLayout.isVisible = false
        binding.spinnerVideo.isVisible = true

        setupList()
        setupVideoFilter()
        observeDanmus()
        viewModel.loadVideoOptions()
    }

    private fun setupVideoFilter() {
        viewModel.videoOptions.observe(this) { options ->
            val titles = options.map { it.title }
            binding.spinnerVideo.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                titles,
            )
            binding.spinnerVideo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.selectVideo(options.getOrNull(position)?.videoId)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
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

    private fun observeDanmus() {
        lifecycleScope.launch {
            viewModel.danmus.collectLatest { adapter.submitData(it) }
        }
    }

    private fun confirmDelete(danmuId: Int) {
        AlertDialog.Builder(this)
            .setMessage(R.string.creator_delete_danmu_confirm)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.deleteDanmu(danmuId) { ok, msg ->
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

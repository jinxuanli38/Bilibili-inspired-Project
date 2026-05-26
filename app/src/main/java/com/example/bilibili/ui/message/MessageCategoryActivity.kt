package com.example.bilibili.ui.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.R
import com.example.bilibili.data.api.MessageService
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.model.UserMessageItem
import com.example.bilibili.databinding.ActivityMessageCategoryBinding
import com.example.bilibili.util.ApiJson.isSuccess
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.ToastUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class MessageCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageCategoryBinding
    private val messageService = RetrofitClient.create(MessageService::class.java)
    private val postService = RetrofitClient.create(PostService::class.java)

    private var pageMode: Int = MODE_REPLY
    private var atMeOnly: Int? = null
    private var pagingJob: Job? = null
    private lateinit var adapter: MessageCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pageMode = intent.getIntExtra(EXTRA_MODE, MODE_REPLY)
        binding.tvTitle.text = when (pageMode) {
            MODE_LIKE -> getString(R.string.message_quick_like)
            MODE_FANS -> getString(R.string.message_quick_fans)
            else -> getString(R.string.message_quick_reply)
        }

        adapter = MessageCategoryAdapter(pageMode) { item ->
            followBack(item)
        }
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClear.setOnClickListener { markAllRead() }
        applyHeaderStyle()
        setupTabsIfNeeded()
        setupSwipeRefresh()
        collectPaging()
    }

    private fun applyHeaderStyle() {
        if (pageMode == MODE_REPLY) {
            binding.dividerHeader.isVisible = false
            binding.layoutTabs.isVisible = true
            return
        }
        binding.layoutTabs.isVisible = false
        binding.dividerHeader.isVisible = true
        binding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            marginStart = 0
        }
    }

    private fun setupTabsIfNeeded() {
        if (pageMode != MODE_REPLY) return
        binding.layoutTabs.isVisible = true
        updateTabUi(allSelected = true)
        binding.tabAll.setOnClickListener {
            if (atMeOnly != null) {
                atMeOnly = null
                updateTabUi(allSelected = true)
                collectPaging()
            }
        }
        binding.tabAtMe.setOnClickListener {
            if (atMeOnly != 1) {
                atMeOnly = 1
                updateTabUi(allSelected = false)
                collectPaging()
            }
        }
    }

    private fun updateTabUi(allSelected: Boolean) {
        if (allSelected) {
            binding.tabAll.setBackgroundResource(R.drawable.bg_message_tab_selected)
            binding.tabAll.setTextColor(getColor(R.color.bili_pink))
            binding.tabAtMe.background = null
            binding.tabAtMe.setTextColor(getColor(R.color.bili_text_grey))
        } else {
            binding.tabAtMe.setBackgroundResource(R.drawable.bg_message_tab_selected)
            binding.tabAtMe.setTextColor(getColor(R.color.bili_pink))
            binding.tabAll.background = null
            binding.tabAll.setTextColor(getColor(R.color.bili_text_grey))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.bili_pink)
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing = state.refresh is LoadState.Loading
                val isEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                binding.tvEmpty.isVisible = isEmpty
            }
        }
    }

    private fun collectPaging() {
        pagingJob?.cancel()
        pagingJob = lifecycleScope.launch {
            createPagerFlow().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun createPagerFlow() = androidx.paging.Pager(
        config = com.example.bilibili.util.PagingDefaults.videoListConfig(),
        pagingSourceFactory = {
            when (pageMode) {
                MODE_LIKE -> MessagePagingSource(
                    messageType = MessageTypes.LIKE,
                    messageTypes = "${MessageTypes.LIKE},${MessageTypes.COLLECTION}",
                )
                MODE_FANS -> MessagePagingSource(
                    messageType = MessageTypes.FANS,
                )
                else -> MessagePagingSource(
                    messageType = MessageTypes.COMMENT,
                    atMeOnly = atMeOnly,
                )
            }
        },
    ).flow

    private fun markAllRead() {
        lifecycleScope.launch {
            try {
                val ok = when (pageMode) {
                    MODE_LIKE -> {
                        val like = JSONObject(messageService.readAll(MessageTypes.LIKE)).isSuccess()
                        val collect = JSONObject(messageService.readAll(MessageTypes.COLLECTION)).isSuccess()
                        like && collect
                    }
                    MODE_FANS -> JSONObject(messageService.readAll(MessageTypes.FANS)).isSuccess()
                    else -> JSONObject(messageService.readAll(MessageTypes.COMMENT)).isSuccess()
                }
                if (ok) {
                    ToastUtils.showShort(this@MessageCategoryActivity, getString(R.string.message_clear_done))
                    adapter.refresh()
                } else {
                    ToastUtils.showShort(this@MessageCategoryActivity, "操作失败")
                }
            } catch (e: Exception) {
                ToastUtils.showShort(this@MessageCategoryActivity, e.message ?: "操作失败")
            }
        }
    }

    private fun followBack(item: UserMessageItem) {
        if (item.sendUserId.isBlank()) return
        lifecycleScope.launch {
            try {
                val response = JSONObject(postService.focus(item.sendUserId))
                if (response.isSuccess()) {
                    ToastUtils.showShort(this@MessageCategoryActivity, getString(R.string.message_follow_back_success))
                } else {
                    ToastUtils.showShort(
                        this@MessageCategoryActivity,
                        response.optString("info", "回关失败"),
                    )
                }
            } catch (e: Exception) {
                ToastUtils.showShort(this@MessageCategoryActivity, e.message ?: "回关失败")
            }
        }
    }

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_REPLY = 1
        const val MODE_LIKE = 2
        const val MODE_FANS = 3

        fun start(context: Context, mode: Int) {
            context.startActivity(
                Intent(context, MessageCategoryActivity::class.java).putExtra(EXTRA_MODE, mode),
            )
        }
    }
}

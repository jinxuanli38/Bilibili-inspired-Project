package com.example.bilibili.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.R
import com.example.bilibili.data.api.MessageService
import com.example.bilibili.databinding.FragmentMessageBinding
import com.example.bilibili.databinding.ItemMessageQuickActionBinding
import com.example.bilibili.util.ApiJson.isSuccess
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val inboxAdapter = MessageInboxAdapter()
    private val messageService = RetrofitClient.create(MessageService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupToolbar()
        setupQuickActions()
        setupInboxList()
    }

    override fun onResume() {
        super.onResume()
        inboxAdapter.refresh()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }
    }

    private fun setupToolbar() {
        binding.btnMarkAllRead.setOnClickListener { markAllRead() }
    }

    private fun setupQuickActions() {
        bindQuickAction(
            ItemMessageQuickActionBinding.bind(binding.quickReply.root),
            R.drawable.bg_message_quick_green,
            R.drawable.ic_message_quick_reply,
            getString(R.string.message_quick_reply),
        )
        bindQuickAction(
            ItemMessageQuickActionBinding.bind(binding.quickLike.root),
            R.drawable.bg_message_quick_pink,
            R.drawable.ic_message_quick_like,
            getString(R.string.message_quick_like),
        )
        bindQuickAction(
            ItemMessageQuickActionBinding.bind(binding.quickFans.root),
            R.drawable.bg_message_quick_blue,
            R.drawable.ic_message_quick_fans,
            getString(R.string.message_quick_fans),
        )
        binding.quickReply.root.setOnClickListener {
            MessageCategoryActivity.start(requireContext(), MessageCategoryActivity.MODE_REPLY)
        }
        binding.quickLike.root.setOnClickListener {
            MessageCategoryActivity.start(requireContext(), MessageCategoryActivity.MODE_LIKE)
        }
        binding.quickFans.root.setOnClickListener {
            MessageCategoryActivity.start(requireContext(), MessageCategoryActivity.MODE_FANS)
        }
    }

    private fun bindQuickAction(
        itemBinding: ItemMessageQuickActionBinding,
        bgRes: Int,
        iconRes: Int,
        label: String,
    ) {
        itemBinding.viewIconBg.setBackgroundResource(bgRes)
        itemBinding.ivIcon.setImageResource(iconRes)
        itemBinding.tvLabel.text = label
    }

    private fun setupInboxList() {
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = inboxAdapter
        binding.swipeRefresh.setColorSchemeResources(R.color.bili_pink)
        binding.swipeRefresh.setOnRefreshListener { inboxAdapter.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    androidx.paging.Pager(
                        config = com.example.bilibili.util.PagingDefaults.videoListConfig(),
                        pagingSourceFactory = { AllMessagePagingSource() },
                    ).flow.collectLatest { pagingData ->
                        inboxAdapter.submitData(pagingData)
                    }
                }
                launch {
                    inboxAdapter.loadStateFlow.collectLatest { state ->
                        binding.swipeRefresh.isRefreshing = state.refresh is LoadState.Loading
                        val isEmpty = state.refresh is LoadState.NotLoading && inboxAdapter.itemCount == 0
                        binding.tvEmpty.isVisible = isEmpty
                    }
                }
            }
        }
    }

    private fun markAllRead() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val types = listOf(
                    MessageTypes.LIKE,
                    MessageTypes.COLLECTION,
                    MessageTypes.COMMENT,
                    MessageTypes.FANS,
                )
                val ok = types.all { type ->
                    JSONObject(messageService.readAll(type)).isSuccess()
                }
                if (ok) {
                    ToastUtils.showShort(requireContext(), getString(R.string.message_clear_done))
                    inboxAdapter.refresh()
                } else {
                    ToastUtils.showShort(requireContext(), "操作失败")
                }
            } catch (e: Exception) {
                ToastUtils.showShort(requireContext(), e.message ?: "操作失败")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

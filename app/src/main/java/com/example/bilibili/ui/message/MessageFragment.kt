package com.example.bilibili.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.MainActivity
import com.example.bilibili.R
import com.example.bilibili.databinding.FragmentMessageBinding
import com.example.bilibili.databinding.ItemMessageQuickActionBinding

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val adapter = MessageConversationAdapter()

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
        setupMessageList()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            (activity as? MainActivity)?.switchToHomeTab()
        }
        binding.btnClear.setOnClickListener {
            // UI 占位，后续接清除未读接口
        }
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

    private fun setupMessageList() {
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter
        adapter.submitList(buildPreviewConversations())
    }

    private fun buildPreviewConversations(): List<MessageConversationItem> = listOf(
        MessageConversationItem(
            name = "创作小助手",
            preview = "[自动回复]你好鸭!感谢关注! 哔哩哔哩(゜-゜)つロ 干杯~",
            time = "5月12日",
            avatarBackgroundRes = R.drawable.bg_message_avatar_system_blue,
            avatarIconRes = R.drawable.ic_message,
            unreadStyle = MessageUnreadStyle.DOT,
        ),
        MessageConversationItem(
            name = "系统通知",
            preview = "双11限时礼包: 年度大会员4.6折, 最高再赠366天",
            time = "5月12日",
            avatarBackgroundRes = R.drawable.bg_message_avatar_system_pink,
            avatarIconRes = R.drawable.ic_message,
            unreadStyle = MessageUnreadStyle.COUNT,
            unreadCount = 1,
        ),
        MessageConversationItem(
            name = "猫嬷王",
            preview = "hi! 我整理了《SpringBoot+Vue》全套源码, 回复【1】领取",
            time = "5月8日",
        ),
        MessageConversationItem(
            name = "小清Eric",
            preview = "求Springboot+vue源码",
            time = "5月8日",
            highlightName = true,
        ),
        MessageConversationItem(
            name = "samwu",
            preview = "关注了你哟!",
            time = "5月8日",
        ),
        MessageConversationItem(
            name = "小清Eric",
            preview = "求Springboot+vue源码",
            time = "5月8日",
            highlightName = true,
        ),
        MessageConversationItem(
            name = "samwu",
            preview = "关注了你哟!",
            time = "5月8日",
        ),
        MessageConversationItem(
            name = "小清Eric",
            preview = "求Springboot+vue源码",
            time = "5月8日",
            highlightName = true,
        ),
        MessageConversationItem(
            name = "samwu",
            preview = "关注了你哟!",
            time = "5月8日",
        ),
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

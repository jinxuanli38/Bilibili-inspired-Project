package com.example.bilibili.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.example.bilibili.R
import com.example.bilibili.databinding.ItemMessageConversationBinding
import com.example.bilibili.util.GlideEngine

class MessageConversationAdapter :
    ListAdapter<MessageConversationItem, MessageConversationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMessageConversationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageConversationItem) {
            binding.tvName.text = item.name
            val context = binding.root.context
            binding.tvName.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (item.highlightName) R.color.bili_pink else R.color.black,
                ),
            )
            binding.tvPreview.text = item.preview
            binding.tvTime.text = item.time

            when (item.unreadStyle) {
                MessageUnreadStyle.DOT -> {
                    binding.viewUnreadDot.isVisible = true
                    binding.tvUnreadCount.isVisible = false
                }
                MessageUnreadStyle.COUNT -> {
                    binding.viewUnreadDot.isVisible = false
                    binding.tvUnreadCount.isVisible = true
                    binding.tvUnreadCount.text = item.unreadCount.coerceAtMost(99).toString()
                }
                MessageUnreadStyle.NONE -> {
                    binding.viewUnreadDot.isVisible = false
                    binding.tvUnreadCount.isVisible = false
                }
            }

            if (item.avatarIconRes != null && item.avatarBackgroundRes != null) {
                binding.ivAvatar.setImageDrawable(null)
                binding.ivAvatar.setBackgroundResource(item.avatarBackgroundRes)
                binding.ivAvatarIcon.isVisible = true
                binding.ivAvatarIcon.setImageResource(item.avatarIconRes)
            } else {
                binding.ivAvatar.background = null
                binding.ivAvatarIcon.isVisible = false
                if (item.avatarRes != null) {
                    binding.ivAvatar.setImageResource(item.avatarRes)
                } else {
                    GlideEngine.loadUserAvatar(binding.root.context, null, binding.ivAvatar)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MessageConversationItem>() {
        override fun areItemsTheSame(
            oldItem: MessageConversationItem,
            newItem: MessageConversationItem,
        ): Boolean = oldItem.name == newItem.name && oldItem.time == newItem.time

        override fun areContentsTheSame(
            oldItem: MessageConversationItem,
            newItem: MessageConversationItem,
        ): Boolean = oldItem == newItem
    }
}

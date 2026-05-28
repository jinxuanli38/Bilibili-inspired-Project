package com.example.bilibili.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.databinding.ItemMessageConversationBinding
import com.example.bilibili.data.model.UserMessageItem
import com.example.bilibili.util.GlideEngine

class MessageInboxAdapter : PagingDataAdapter<UserMessageItem, MessageInboxAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    class ViewHolder(
        private val binding: ItemMessageConversationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserMessageItem) {
            val context = binding.root.context
            binding.tvName.text = item.sendUserName
            binding.tvPreview.text = MessagePreviewFormatter.preview(context, item)
            binding.tvTime.text = MessageTimeFormatter.format(item.createTimeRaw)
            binding.viewUnreadDot.visibility = View.GONE
            binding.tvUnreadCount.visibility = View.GONE
            binding.ivAvatarIcon.visibility = View.GONE
            binding.ivAvatar.background = null
            GlideEngine.loadUserAvatar(context, item.sendUserAvatar, binding.ivAvatar)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserMessageItem>() {
        override fun areItemsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem == newItem
    }
}

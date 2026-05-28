package com.example.bilibili.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserMessageItem
import com.example.bilibili.databinding.ItemMessageFanBinding
import com.example.bilibili.databinding.ItemMessageReplyBinding
import com.example.bilibili.util.GlideEngine

class MessageCategoryAdapter(
    private val pageMode: Int,
    private val onFollowBack: (UserMessageItem) -> Unit,
) : PagingDataAdapter<UserMessageItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int = pageMode

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MessageCategoryActivity.MODE_FANS -> FanHolder(
                ItemMessageFanBinding.inflate(inflater, parent, false),
                onFollowBack,
            )
            else -> ReplyHolder(ItemMessageReplyBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder) {
            is ReplyHolder -> holder.bind(item)
            is FanHolder -> holder.bind(item)
        }
    }

    private class ReplyHolder(
        private val binding: ItemMessageReplyBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserMessageItem) {
            val context = binding.root.context
            binding.tvName.text = item.sendUserName
            binding.tvAction.text = context.getString(R.string.message_action_reply_comment)
            binding.tvContent.text = item.messageContent
            binding.tvTime.text = MessageTimeFormatter.format(item.createTimeRaw)
            GlideEngine.loadUserAvatar(context, item.sendUserAvatar, binding.ivAvatar)
            binding.tvPreview.text = item.messageContentReply
        }
    }

    private class FanHolder(
        private val binding: ItemMessageFanBinding,
        private val onFollowBack: (UserMessageItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserMessageItem) {
            binding.tvName.text = item.sendUserName
            binding.tvTime.text = MessageTimeFormatter.format(item.createTimeRaw)
            GlideEngine.loadUserAvatar(binding.root.context, item.sendUserAvatar, binding.ivAvatar)
            binding.btnFollowBack.setOnClickListener { onFollowBack(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserMessageItem>() {
        override fun areItemsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem == newItem
    }
}

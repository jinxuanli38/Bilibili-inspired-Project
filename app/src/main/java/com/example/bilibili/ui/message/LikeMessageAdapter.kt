package com.example.bilibili.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserMessageItem
import com.example.bilibili.databinding.ItemMessageLikeBinding
import com.example.bilibili.util.GlideEngine

class LikeMessageAdapter : PagingDataAdapter<UserMessageItem, LikeMessageAdapter.LikeHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeHolder {
        val binding = ItemMessageLikeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return LikeHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    class LikeHolder(
        private val binding: ItemMessageLikeBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserMessageItem) {
            val context = binding.root.context
            binding.tvNames.text = item.sendUserName
            binding.tvSummary.text = summaryText(context, item)
            binding.tvTime.text = MessageTimeFormatter.format(item.createTimeRaw)
            GlideEngine.loadUserAvatar(context, item.sendUserAvatar, binding.ivAvatar)

            val previewText = item.previewTextForLike()
            val showCover = !item.isCommentTarget && item.videoCover.isNotBlank()
            binding.ivPreviewCover.isVisible = showCover
            binding.tvPreview.isVisible = !showCover
            if (showCover) {
                GlideEngine.loadVideoCover(context, item.videoCover, binding.ivPreviewCover)
            } else if (previewText.isNotBlank()) {
                binding.tvPreview.text = previewText
            } else {
                binding.tvPreview.text = ""
            }
        }

        private fun summaryText(context: android.content.Context, item: UserMessageItem): String {
            return when {
                item.messageType == MessageTypes.COLLECTION ->
                    context.getString(R.string.message_collected_my_video)
                item.isCommentTarget ->
                    context.getString(R.string.message_liked_my_comment)
                else ->
                    context.getString(R.string.message_liked_my_video)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserMessageItem>() {
        override fun areItemsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: UserMessageItem, newItem: UserMessageItem): Boolean =
            oldItem == newItem
    }
}

package com.example.bilibili.ui.creator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CreatorCommentItem
import com.example.bilibili.databinding.ItemCreatorInteractionBinding

class CreatorCommentAdapter(
    private val onDelete: (CreatorCommentItem) -> Unit,
) : PagingDataAdapter<CreatorCommentItem, CreatorCommentAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCreatorInteractionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
        holder.binding.btnDelete.setOnClickListener { onDelete(item) }
    }

    class ViewHolder(
        val binding: ItemCreatorInteractionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CreatorCommentItem) {
            binding.tvContent.text = item.content
            binding.tvVideo.text = "来自：${item.videoName}"
            binding.tvMeta.text = "${item.nickName} · ${item.postTime}"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CreatorCommentItem>() {
        override fun areItemsTheSame(oldItem: CreatorCommentItem, newItem: CreatorCommentItem) =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: CreatorCommentItem, newItem: CreatorCommentItem) =
            oldItem == newItem
    }
}

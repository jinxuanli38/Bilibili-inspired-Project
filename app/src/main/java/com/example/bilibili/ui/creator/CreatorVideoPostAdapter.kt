package com.example.bilibili.ui.creator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CreatorVideoPost
import com.example.bilibili.databinding.ItemCreatorVideoPostBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.VideoDataUtils

class CreatorVideoPostAdapter(
    private val onDelete: (CreatorVideoPost) -> Unit,
) : PagingDataAdapter<CreatorVideoPost, CreatorVideoPostAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCreatorVideoPostBinding.inflate(
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
        val binding: ItemCreatorVideoPostBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CreatorVideoPost) {
            binding.tvTitle.text = item.videoName
            binding.tvStatus.text = item.statusLabel
            binding.tvMeta.text = "播放 ${VideoDataUtils.formatCount(item.playCount)} · ${item.createTime}"
            GlideEngine.loadVideoCover(binding.root.context, item.videoCover, binding.ivCover)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CreatorVideoPost>() {
        override fun areItemsTheSame(oldItem: CreatorVideoPost, newItem: CreatorVideoPost) =
            oldItem.videoId == newItem.videoId

        override fun areContentsTheSame(oldItem: CreatorVideoPost, newItem: CreatorVideoPost) =
            oldItem == newItem
    }
}

package com.example.bilibili.ui.creator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CreatorDanmuItem
import com.example.bilibili.databinding.ItemCreatorInteractionBinding

class CreatorDanmuAdapter(
    private val onDelete: (CreatorDanmuItem) -> Unit,
) : PagingDataAdapter<CreatorDanmuItem, CreatorDanmuAdapter.ViewHolder>(DiffCallback) {

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
        fun bind(item: CreatorDanmuItem) {
            binding.tvContent.text = item.text
            binding.tvVideo.text = "来自：${item.videoName}"
            binding.tvMeta.text = "${item.nickName} · ${item.postTime}"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CreatorDanmuItem>() {
        override fun areItemsTheSame(oldItem: CreatorDanmuItem, newItem: CreatorDanmuItem) =
            oldItem.danmuId == newItem.danmuId

        override fun areContentsTheSame(oldItem: CreatorDanmuItem, newItem: CreatorDanmuItem) =
            oldItem == newItem
    }
}

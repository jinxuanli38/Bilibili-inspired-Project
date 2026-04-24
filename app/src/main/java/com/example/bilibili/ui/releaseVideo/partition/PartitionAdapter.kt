package com.example.bilibili.ui.releaseVideo.partition

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CategoryInfo
import com.example.bilibili.databinding.ItemPartitionBinding

class PartitionAdapter(
    private val onItemClick: (CategoryInfo) -> Unit
) : ListAdapter<CategoryInfo, PartitionAdapter.ViewHolder>(PartitionDiffCallback()) {

    // 1. 初始化为 null
    private var selectedPartition: CategoryInfo? = null

    // 2. 更新方法也要支持可空逻辑
    fun setSelected(item: CategoryInfo?) {
        if (selectedPartition != item) {
            val oldSelected = selectedPartition
            selectedPartition = item

            // 刷新旧的
            val oldIndex = currentList.indexOf(oldSelected)
            if (oldIndex != -1) notifyItemChanged(oldIndex)

            // 刷新新的
            val newIndex = currentList.indexOf(item)
            if (newIndex != -1) notifyItemChanged(newIndex)
        }
    }

    class ViewHolder(val binding: ItemPartitionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartitionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvPartitionName.text = item.categoryName

        // 利用之前的 selector，通过 isSelected 状态切换样式（主要是比较ID）
        holder.binding.tvPartitionName.isSelected = (item.categoryId == selectedPartition?.categoryId)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    // DiffUtil 计算逻辑
    class PartitionDiffCallback : DiffUtil.ItemCallback<CategoryInfo>() {
        // 比较 ID，确定是不是同一个格子
        override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }

        // 比较内容，确定格子的 UI 是否需要重绘
        // 因为 CategoryInfo 是 data class，用 == 比较的就是所有字段
        override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
            return oldItem == newItem
        }
    }
}
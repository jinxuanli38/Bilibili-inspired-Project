package com.example.bilibili.ui.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.CreatorStatItem
import com.example.bilibili.databinding.ItemCreatorStatCellBinding
import com.example.bilibili.util.VideoDataUtils

class CreatorStatAdapter(
    private val onItemClick: ((CreatorStatItem) -> Unit)? = null,
) : ListAdapter<CreatorStatItem, CreatorStatAdapter.ViewHolder>(DiffCallback) {

    private var selectedDataType: Int = 1

    fun setSelectedDataType(dataType: Int) {
        if (selectedDataType != dataType) {
            selectedDataType = dataType
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCreatorStatCellBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.dataType == selectedDataType)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    class ViewHolder(
        private val binding: ItemCreatorStatCellBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CreatorStatItem, selected: Boolean) {
            binding.tvLabel.text = item.label
            binding.tvValue.text = VideoDataUtils.formatCount(item.total)
            binding.tvYesterday.text = binding.root.context.getString(
                R.string.creator_stat_yesterday_delta,
                item.yesterdayDelta,
            )
            // 触发 XML selector（背景 + 文字颜色）
            binding.root.isSelected = selected
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CreatorStatItem>() {
        override fun areItemsTheSame(oldItem: CreatorStatItem, newItem: CreatorStatItem) =
            oldItem.dataType == newItem.dataType

        override fun areContentsTheSame(oldItem: CreatorStatItem, newItem: CreatorStatItem) =
            oldItem == newItem
    }
}

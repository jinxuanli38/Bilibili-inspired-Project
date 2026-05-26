package com.example.bilibili.ui.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CreatorServiceEntry
import com.example.bilibili.databinding.ItemCreatorServiceBinding

class CreatorServiceAdapter(
    private val onItemClick: (CreatorServiceEntry) -> Unit,
) : ListAdapter<CreatorServiceEntry, CreatorServiceAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCreatorServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class ViewHolder(
        private val binding: ItemCreatorServiceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CreatorServiceEntry) {
            binding.tvTitle.text = item.title
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CreatorServiceEntry>() {
        override fun areItemsTheSame(oldItem: CreatorServiceEntry, newItem: CreatorServiceEntry) =
            oldItem.action == newItem.action

        override fun areContentsTheSame(oldItem: CreatorServiceEntry, newItem: CreatorServiceEntry) =
            oldItem == newItem
    }
}

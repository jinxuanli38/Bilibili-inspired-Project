package com.example.bilibili.ui.personal.contribute

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.VideoItem
import com.example.bilibili.databinding.ItemVideoContributeGridBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient

class ContributeVideoAdapter(private val onClick: (VideoItem) -> Unit) :
    RecyclerView.Adapter<ContributeVideoAdapter.VideoViewHolder>() {

    private var items = listOf<VideoItem>()

    fun submitList(newList: List<VideoItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        // 注意：这里使用你指定的 item_video_contribute_grid 布局
        val binding = ItemVideoContributeGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    class VideoViewHolder(private val binding: ItemVideoContributeGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoItem) {
            binding.tvVideoTitle.text = video.videoName
            binding.tvPlayCount.text = formatCount(video.playCount)
            binding.tvCommentCount.text = formatCount(video.danmuCount)

            // 格式化时长 (秒 -> 00:00)
            binding.tvDuration.text = formatDuration(video.duration)

            // 直接传递封面路径，让GlideEngine自动拼接URL
            GlideEngine.loadVideoCover(binding.root.context, video.videoCover, binding.ivVideoCover)
        }

        private fun formatCount(count: Int): String =
            if (count >= 10000) "${count / 10000}万" else count.toString()

        private fun formatDuration(seconds: Int): String {
            val min = seconds / 60
            val sec = seconds % 60
            return String.format("%02d:%02d", min, sec)
        }
    }
}
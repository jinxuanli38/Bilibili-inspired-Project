package com.example.bilibili.ui.personal.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.VideoItem
import com.example.bilibili.databinding.ItemVideoHomeGridBinding
import com.example.bilibili.util.GlideEngine

class HomeVideoAdapter(private val onClick: (VideoItem) -> Unit) :
    RecyclerView.Adapter<HomeVideoAdapter.VideoViewHolder>() {

    private var videoList = emptyList<VideoItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<VideoItem>) {
        this.videoList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = videoList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoHomeGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // 1. 在这里创建 ViewHolder，并把点击逻辑传进去
        return VideoViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        // 2. 将当前的数据对象传给 bind 方法
        holder.bind(videoList[position])
    }

    class VideoViewHolder(
        val binding: ItemVideoHomeGridBinding,
        private val onClick: (VideoItem) -> Unit // 传入点击回调
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentVideo: VideoItem? = null

        init {
            // 3. 点击事件只设置一次，性能更好
            binding.root.setOnClickListener {
                currentVideo?.let { onClick(it) }
            }
        }

        @SuppressLint("DefaultLocale")
        fun bind(video: VideoItem) {
            this.currentVideo = video // 保存当前数据

            binding.tvVideoTitle.text = video.videoName

            // 建议：如果数量过万，可以处理成 "1.2万" 这种格式，更像B站
            binding.tvPlayCount.text = video.playCount.toString()
            binding.tvCommentCount.text = video.danmuCount.toString()

            // 格式化时间
            val totalSeconds = video.duration
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            binding.tvDuration.text = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }

            GlideEngine.loadVideoCover(binding.root.context, video.videoCover, binding.ivVideoCover)
        }
    }
}
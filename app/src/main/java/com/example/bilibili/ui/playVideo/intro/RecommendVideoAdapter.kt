package com.example.bilibili.ui.playVideo.intro

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.databinding.ItemVideoRecommendGridBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.VideoDataUtils

/**
 * 推荐视频适配器
 */
class RecommendVideoAdapter(
    private val onVideoClick: (RecommendVideoItem) -> Unit
) : ListAdapter<RecommendVideoItem, RecommendVideoAdapter.VideoViewHolder>(VideoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoRecommendGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(private val binding: ItemVideoRecommendGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendVideoItem) {
            binding.apply {
                // 关键词高亮处理
                val rawTitle = item.videoName ?: ""

                val formattedTitle = rawTitle
                    .replace("<span class='highlight'>", "<font color='#FB7299'>")
                    .replace("</span>", "</font>")

                // 使用 Html.fromHtml 渲染颜色
                tvVideoTitle.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    android.text.Html.fromHtml(formattedTitle, android.text.Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    android.text.Html.fromHtml(formattedTitle)
                }

                tvUploaderName.text = item.nickName
                tvPlayCount.text = VideoDataUtils.formatCount(item.playCount)
                tvCommentCount.text = VideoDataUtils.formatCount(item.danmuCount)

                GlideEngine.loadVideoCover(root.context, item.videoCover, ivVideoCover)

                val durationSeconds = item.duration
                if (durationSeconds != null && durationSeconds > 0) {
                    tvDuration.text = VideoDataUtils.formatDuration(durationSeconds)
                    tvDuration.visibility = android.view.View.VISIBLE
                } else {
                    tvDuration.visibility = android.view.View.GONE
                }

                root.setOnClickListener { onVideoClick(item) }
            }
        }

    }
}

/**
 * 推荐视频数据模型
 */
data class RecommendVideoItem(
    val videoId: String = "",
    val videoName: String = "",
    val videoCover: String = "",
    val nickName: String = "",
    val userId: String = "",
    val playCount: Int = 0,
    val danmuCount: Int = 0,
    val commentCount: Int = 0,
    val duration: Int? = null
)

/**
 * 差异计算回调
 */
object VideoDiffCallback : DiffUtil.ItemCallback<RecommendVideoItem>() {
    override fun areItemsTheSame(oldItem: RecommendVideoItem, newItem: RecommendVideoItem): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: RecommendVideoItem, newItem: RecommendVideoItem): Boolean {
        return oldItem == newItem
    }
}
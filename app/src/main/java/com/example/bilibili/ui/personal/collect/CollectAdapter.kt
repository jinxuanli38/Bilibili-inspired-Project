package com.example.bilibili.ui.personal.collect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.model.CollectVideo
import com.example.bilibili.databinding.ItemVideoCollectGridBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient

class CollectAdapter(private val onClick: (CollectVideo) -> Unit) :
    RecyclerView.Adapter<CollectAdapter.ViewHolder>() {

    private var items = listOf<CollectVideo>()

    fun setData(newList: List<CollectVideo>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoCollectGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = items[position]
        holder.binding.apply {
            // 1. 设置标题
            tvVideoTitle.text = video.videoName

            // 2. 设置 UP 主 UID
            tvUpName.text = "来自 UID: ${video.videoUserId}"

            // 3. 设置封面右下角的收藏日期
            // 截取 2026-04-22 17:19:10 前面的日期
            tvActionDate.text = video.actionTime.substringBefore(" ")

            // 4. 设置收藏热度
            tvActionCount.text = "收藏热度: ${video.actionCount}"

            // 5. 加载封面 (使用你自己的 Glide 引擎)
            // 直接传递封面路径，让GlideEngine自动拼接URL
            GlideEngine.loadVideoCover(ivVideoCover.context, video.videoCover, ivVideoCover)

            // 6. 点击跳转
            root.setOnClickListener { onClick(video) }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemVideoCollectGridBinding) : RecyclerView.ViewHolder(binding.root)
}
package com.example.bilibili.ui.focus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.databinding.ItemFriendBinding
import com.example.bilibili.util.GlideEngine

class FocusOnAdapter(
    private var list: List<UserFriend>,
    private val onActionClick: (UserFriend) -> Unit
) : RecyclerView.Adapter<FocusOnAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        val binding = holder.binding

        // 1. 设置基础信息
        binding.tvNickname.text = user.otherNickName
        binding.tvDescription.text = user.otherPersonalIntroduction ?: "暂无简介"
        GlideEngine.loadUserAvatar(binding.root.context, user.otherAvatar, binding.ivAvatar)

        // 2. 根据 focusType 设置按钮文字（你的 JSON 里 0 可能代表已关注）
        binding.btnFollowAction.apply {
            // 统一样式：B站关注列表按钮通常是灰色边框
            setTextColor(Color.parseColor("#999999"))
            setBackgroundResource(R.drawable.shape_follow_btn_grey) // 记得创建这个 shape

            // 假设 1 是互相关注，其他是已关注
            text = if (user.focusType == 1) "互相关注" else "已关注"

            setOnClickListener { onActionClick(user) }
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<UserFriend>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
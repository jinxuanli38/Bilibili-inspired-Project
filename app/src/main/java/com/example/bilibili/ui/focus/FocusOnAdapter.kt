package com.example.bilibili.ui.focus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.databinding.ItemFriendBinding
import com.example.bilibili.util.FollowActionButtonUi
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.UserInfoText

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
        binding.tvNickname.setTextColor(
            if (user.focusType == 1) Color.parseColor("#FB7299") else Color.parseColor("#212121"),
        )
        binding.tvDescription.text = UserInfoText.displayIntroduction(user.otherPersonalIntroduction)
        GlideEngine.loadUserAvatar(binding.root.context, user.otherAvatar, binding.ivAvatar)

        // 2. 根据 focusType 设置按钮文字和样式
        binding.btnFollowAction.apply {
            FollowActionButtonUi.bind(this, user.focusType)
            setOnClickListener { onActionClick(user) }
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<UserFriend>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
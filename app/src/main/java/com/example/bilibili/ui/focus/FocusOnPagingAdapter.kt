package com.example.bilibili.ui.focus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.databinding.ItemFriendBinding
import com.example.bilibili.util.FollowActionButtonUi
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.UserInfoText

class FocusOnPagingAdapter(
    private val onActionClick: (UserFriend) -> Unit,
    private val onUserClick: (UserFriend) -> Unit,
) : PagingDataAdapter<UserFriend, FocusOnPagingAdapter.ViewHolder>(UserFriendDiffCallback()) {

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position) ?: return
        val binding = holder.binding

        binding.tvNickname.text = user.otherNickName
        binding.tvNickname.setTextColor(
            if (user.focusType == 1) Color.parseColor("#FB7299") else Color.parseColor("#212121"),
        )
        binding.tvDescription.text = UserInfoText.displayIntroduction(user.otherPersonalIntroduction)
        GlideEngine.loadUserAvatar(binding.root.context, user.otherAvatar, binding.ivAvatar)

        binding.root.setOnClickListener { onUserClick(user) }
        binding.ivAvatar.setOnClickListener { onUserClick(user) }
        binding.ivMore.visibility = View.GONE

        binding.btnFollowAction.apply {
            FollowActionButtonUi.bindForFollowingList(this, user.focusType)
            setOnClickListener { onActionClick(user) }
        }
    }

    class UserFriendDiffCallback : DiffUtil.ItemCallback<UserFriend>() {
        override fun areItemsTheSame(oldItem: UserFriend, newItem: UserFriend): Boolean {
            return oldItem.otherUserId == newItem.otherUserId
        }

        override fun areContentsTheSame(oldItem: UserFriend, newItem: UserFriend): Boolean {
            return oldItem == newItem
        }
    }
}

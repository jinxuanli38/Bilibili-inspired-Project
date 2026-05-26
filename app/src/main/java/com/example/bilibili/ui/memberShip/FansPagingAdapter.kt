package com.example.bilibili.ui.memberShip

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
class FansPagingAdapter(
    private val onActionClick: (UserFriend) -> Unit,
    private val onUserClick: (UserFriend) -> Unit,
    private val onMoreClick: (UserFriend) -> Unit,
    private val showMoreMenu: Boolean = true,
) : PagingDataAdapter<UserFriend, FansPagingAdapter.ViewHolder>(UserFriendDiffCallback()) {

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position) ?: return
        val binding = holder.binding

        binding.tvNickname.text = user.otherNickName
        binding.tvNickname.setTextColor(Color.parseColor("#212121"))
        binding.tvDescription.text = binding.root.context.getString(
            R.string.friend_fan_video_stats,
            user.otherFansCount,
            user.otherVideoCount,
        )
        GlideEngine.loadUserAvatar(binding.root.context, user.otherAvatar, binding.ivAvatar)

        binding.root.setOnClickListener { onUserClick(user) }
        binding.ivAvatar.setOnClickListener { onUserClick(user) }
        binding.ivMore.visibility = if (showMoreMenu) View.VISIBLE else View.GONE
        binding.ivMore.setOnClickListener { if (showMoreMenu) onMoreClick(user) }

        when (user.focusType) {
            1 -> FollowActionButtonUi.bind(binding.btnFollowAction, 1)
            0 -> binding.btnFollowAction.apply {
                setCompoundDrawablesRelative(null, null, null, null)
                setTextColor(Color.parseColor("#FB7299"))
                setBackgroundResource(R.drawable.shape_follow_btn_pink)
                text = "回关"
            }
            else -> FollowActionButtonUi.bind(binding.btnFollowAction, 2)
        }
        binding.btnFollowAction.setOnClickListener { onActionClick(user) }
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

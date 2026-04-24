package com.example.bilibili.ui.personal.fans

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.databinding.ItemFriendBinding
import com.example.bilibili.util.GlideEngine

class FansAdapter(
    private var list: List<UserFriend>,
    private val onBtnClick: (UserFriend) -> Unit
) : RecyclerView.Adapter<FansAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        val binding = holder.binding

        // 1. 加载头像和文本信息
        GlideEngine.loadUserAvatar(binding.root.context, user.otherAvatar, binding.ivAvatar)
        binding.tvNickname.text = user.otherNickName
        binding.tvDescription.text = user.otherPersonalIntroduction

        // 2. 粉丝列表状态判断：只有"回关"按钮
        binding.btnFollowAction.apply {
            // 样式统一：粉色文字 + 粉色边框 + 加号图标
            setTextColor(Color.parseColor("#FB7299"))
            setBackgroundResource(R.drawable.shape_follow_btn_pink)
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

            // 粉丝列表通常显示"回关"按钮
            text = "回关"
        }

        // 3. 点击回调：在粉丝列表点这个通常是执行"回关"
        binding.btnFollowAction.setOnClickListener { onBtnClick(user) }
    }

    override fun getItemCount() = list.size

    // 刷新数据的方法
    fun updateData(newList: List<UserFriend>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
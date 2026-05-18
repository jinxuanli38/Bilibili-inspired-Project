package com.example.bilibili.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bilibili.R
import com.example.bilibili.data.api.PostService
import com.example.bilibili.databinding.ActivityUserProfileBinding
import com.example.bilibili.ui.personal.contribute.ContributeFragment
import com.example.bilibili.ui.personal.home.HomeFragment
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.ToastUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: UserProfileViewModel by viewModels()

    private val postService = RetrofitClient.create(PostService::class.java)

    // 只保留主页和投稿两个 tab
    private val tabTitles = listOf("主页", "投稿")

    // 目标用户ID
    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取目标用户ID
        targetUserId = intent.getStringExtra("user_id")
        if (targetUserId.isNullOrEmpty()) {
            ToastUtils.showShort(this, "用户信息加载失败")
            finish()
            return
        }

        // 设置状态栏适配
        setupStatusBarPadding()

        // 初始化布局
        setupViewPagerAndTabs()

        // 加载用户信息
        loadUserInfo()

        // 观察数据变化
        observeData()

        // 返回按钮
        binding.ivBack.setOnClickListener { finish() }

        // 关注按钮
        binding.btnFocus.setOnClickListener {
            viewModel.toggleFocus(targetUserId!!)
        }
    }

    private fun setupStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.root.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
    }

    private fun setupViewPagerAndTabs() {
        binding.viewPager.adapter = UserProfilePagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    postService.getUserInfo(targetUserId!!)
                }

                val jsonObject = JSONObject(result)
                if (jsonObject.optInt("code") == 200) {
                    val userData = jsonObject.getJSONObject("data")
                    viewModel.setUserInfo(userData)
                } else {
                    val errorMsg = jsonObject.optString("message", "加载用户信息失败")
                    ToastUtils.showShort(this@UserProfileActivity, errorMsg)
                }
            } catch (e: Exception) {
                ToastUtils.showShort(this@UserProfileActivity, "网络错误，请重试")
                e.printStackTrace()
            }
        }
    }

    private fun observeData() {
        viewModel.userInfo.observe(this) { userInfo ->
            // 更新UI
            userInfo?.let { updateUI(it) }
        }

        viewModel.focusState.observe(this) { isFocused ->
            // 更新关注按钮状态
            updateFocusButton(isFocused)
        }
    }

    private fun updateUI(userInfo: UserInfo) {
        // 加载头像
        GlideEngine.loadUserAvatar(this, userInfo.avatar, binding.ivAvatar)

        // 设置昵称
        binding.tvNickname.text = userInfo.nickName

        // 设置简介
        binding.tvDescription.text = if (userInfo.personalIntroduction.isNullOrEmpty()) {
            "这个人很懒，什么都没留下"
        } else {
            userInfo.personalIntroduction
        }

        // 设置统计数据
        binding.tvFansCount.text = userInfo.fansCount.toString()
        binding.tvFollowCount.text = userInfo.focusCount.toString()
        binding.tvLikeCount.text = userInfo.likeCount.toString()

        // 设置关注状态
        viewModel.setFocused(userInfo.haveFocus)

        // 显示内容，隐藏加载遮罩
        binding.loadingView.visibility = View.GONE
    }

    private fun updateFocusButton(isFocused: Boolean) {
        if (isFocused) {
            binding.btnFocus.text = "已关注"
            binding.btnFocus.setBackgroundColor(resources.getColor(R.color.sl_divider_color))
            binding.btnFocus.setTextColor(resources.getColor(R.color.sl_comment_item_color_default))
        } else {
            binding.btnFocus.text = "关注"
            binding.btnFocus.setBackgroundColor(resources.getColor(R.color.bilibili_pink))
            binding.btnFocus.setTextColor(resources.getColor(R.color.white))
        }
    }

    inner class UserProfilePagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> {
                    // 主页 Fragment，传递用户ID
                    val fragment = HomeFragment()
                    fragment.arguments = Bundle().apply {
                        putString("user_id", targetUserId)
                    }
                    fragment
                }
                1 -> {
                    // 投稿 Fragment，传递用户ID
                    val fragment = ContributeFragment()
                    fragment.arguments = Bundle().apply {
                        putString("user_id", targetUserId)
                    }
                    fragment
                }
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 不需要清除 binding，因为这是一个 Activity
    }
}
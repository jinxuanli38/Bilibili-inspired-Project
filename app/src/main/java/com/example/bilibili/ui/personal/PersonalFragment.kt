package com.example.bilibili.ui.personal

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bilibili.R
import com.example.bilibili.data.api.PostService
import com.example.bilibili.databinding.FragmentPersonalBinding
import com.example.bilibili.ui.edit.EditActivity
import com.example.bilibili.ui.login.LoginActivity
import com.example.bilibili.ui.personal.collect.CollectFragment
import com.example.bilibili.ui.personal.contribute.ContributeFragment
import com.example.bilibili.ui.personal.home.HomeFragment
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.SPUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PersonalFragment : Fragment() {
    private var _binding: FragmentPersonalBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("主页", "投稿", "收藏")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 添加状态栏适配
        setupStatusBarPadding()

        setupViewPagerAndTabs()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val responseString = withContext(Dispatchers.IO) {
                    // 获取个人信息
                    val postService = RetrofitClient.create(PostService::class.java)
                    postService.getUserInfo(SPUtils.getUserId())
                }
                val userInfo = JSONObject(responseString)
                if (userInfo.optInt("code") == 200) {
                    val data = userInfo.getJSONObject("data")
                    // 更新个人信息
                    binding.apply {
                        // 获取并更新头像
                        val avatar = data.optString("avatar", "")
                        SPUtils.saveAvatar(avatar) // 更新本地存储的头像
                        GlideEngine.loadUserAvatar(requireContext(), avatar, ivAvatar)
                        // 粉丝数量
                        tvFansCount.text = data.optInt("fansCount").toString()
                        // 关注数量
                        tvFollowCount.text = data.optInt("focusCount").toString()
                        // 获赞数量
                        tvLikeCount.text = data.optInt("likeCount").toString()
                        // 昵称
                        tvNickname.text = data.getString("nickName")
                        // 个人描述
                        tvDescription.text = data.getString("personalIntroduction")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.loadingView.visibility = View.GONE
            }
        }

        binding.btnLogout.setOnClickListener {
            // 跳转到编辑资料页面
            val intent = Intent(requireContext(), EditActivity::class.java)
            startActivity(intent)
        }

        // 设置退出登录按钮
        setupLogoutButton()
    } // 🔥 修复点1：在这里加上右大括号，正确结束 onViewCreated 方法

    private fun setupStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            // 只给顶部banner添加状态栏padding
            val params = binding.ivBanner.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.topMargin = statusBarHeight
            binding.ivBanner.layoutParams = params

            insets
        }
    }

    private fun setupLogoutButton() {
        // 检查是否是当前用户
        val currentUserId = SPUtils.getUserId()
        val isCurrentUser = (currentUserId.isNotEmpty() && currentUserId == "self")

        if (isCurrentUser) {
            // 显示退出登录按钮
            binding.ivLogout.visibility = View.VISIBLE
            binding.ivLogout.setOnClickListener {
                showLogoutDialog()
            }
        } else {
            // 隐藏退出登录按钮
            binding.ivLogout.visibility = View.GONE
        }
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定") { _, _ ->
                // 退出登录逻辑
                SPUtils.cleanToken()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()

        // 设置按钮颜色为粉色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.bilibili_pink, null))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.bilibili_pink, null))
    }

    private fun setupViewPagerAndTabs() {
        binding.viewPager.adapter = UserProfilePagerAdapter(requireActivity())
        binding.viewPager.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = PersonalFragment()
    }

    fun switchToContributeTab() {
        // 切换到投稿tab（position 1）
        binding.viewPager.setCurrentItem(1, true)
    }

    inner class UserProfilePagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment.newInstance()
                1 -> ContributeFragment.newInstance()
                2 -> CollectFragment.newInstance()
                else -> HomeFragment.newInstance()
            }
        }
    }
}
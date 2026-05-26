package com.example.bilibili.ui.friends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bilibili.databinding.ActivityMyFriendsBinding
import com.example.bilibili.ui.focus.FollowingListFragment
import com.example.bilibili.ui.memberShip.FansListFragment
import com.google.android.material.tabs.TabLayoutMediator

class MyFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFriendsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val startTab = intent.getIntExtra(EXTRA_TAB_INDEX, TAB_FOLLOWING)

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return if (position == TAB_FOLLOWING) FollowingListFragment() else FansListFragment()
            }
        }
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.setCurrentItem(startTab, false)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == TAB_FOLLOWING) "关注" else "粉丝"
        }.attach()
    }

    companion object {
        const val EXTRA_TAB_INDEX = "tab_index"
        const val TAB_FOLLOWING = 0
        const val TAB_FANS = 1

        fun start(context: Context, tabIndex: Int = TAB_FOLLOWING) {
            context.startActivity(
                Intent(context, MyFriendsActivity::class.java).apply {
                    putExtra(EXTRA_TAB_INDEX, tabIndex)
                },
            )
        }
    }
}

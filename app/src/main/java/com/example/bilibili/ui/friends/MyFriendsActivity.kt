package com.example.bilibili.ui.friends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bilibili.databinding.ActivityMyFriendsBinding
import com.example.bilibili.ui.focus.FollowingListFragment
import com.example.bilibili.ui.memberShip.FansListFragment
import com.example.bilibili.ui.user.FollowStatsCenter
import com.example.bilibili.util.FollowRelationRefreshTracker
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFriendsBinding
    private val friendsViewModel: MyFriendsViewModel by viewModels()
    private val followRefreshTracker = FollowRelationRefreshTracker()

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

        observeFollowRelationChanges()
        followRefreshTracker.sync()
    }

    override fun onPause() {
        followRefreshTracker.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        followRefreshTracker.onResumeIfChanged {
            friendsViewModel.notifyFollowRelationChanged()
        }
    }

    private fun observeFollowRelationChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                FollowStatsCenter.changes.collectLatest {
                    friendsViewModel.notifyFollowRelationChanged()
                }
            }
        }
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

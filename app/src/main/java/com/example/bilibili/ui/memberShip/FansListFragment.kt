package com.example.bilibili.ui.memberShip

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.R
import com.example.bilibili.data.model.UserFriend
import com.example.bilibili.databinding.DialogFanMoreBinding
import com.example.bilibili.databinding.FragmentFriendListTabBinding
import com.example.bilibili.ui.friends.MyFriendsViewModel
import com.example.bilibili.ui.user.UserProfileActivity
import com.example.bilibili.util.FollowConfirmDialog
import com.example.bilibili.util.FollowRelationRefreshTracker
import com.example.bilibili.util.PagingUiHelper
import com.example.bilibili.util.BilibiliBottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** 我的好友 - 粉丝 Tab */
class FansListFragment : Fragment() {

    private var _binding: FragmentFriendListTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FansViewModel by viewModels()
    private val friendsViewModel: MyFriendsViewModel by activityViewModels()
    private lateinit var adapter: FansPagingAdapter
    private val followRefreshTracker = FollowRelationRefreshTracker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFriendListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateFansSummary(0)

        adapter = FansPagingAdapter(
            onActionClick = { user ->
                if (user.focusType == 0) {
                    showFollowConfirmDialog(user)
                } else {
                    showCancelFollowDialog(user)
                }
            },
            onUserClick = { user -> openUserProfile(user.otherUserId) },
            onMoreClick = { user -> showFanMoreBottomSheet(user) },
        )

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#FB7299"))
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fansList.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        PagingUiHelper.bindEmptyState(
            viewLifecycleOwner,
            binding.emptyState.llEmpty,
            binding.rvList,
            adapter,
        ) { loadState ->
            binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
            if (loadState.refresh is LoadState.NotLoading) {
                updateFansSummary(adapter.itemCount)
            }
        }

        observeSharedListRefresh()
        followRefreshTracker.sync()
    }

    override fun onPause() {
        followRefreshTracker.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        followRefreshTracker.onResumeIfChanged {
            if (_binding != null && ::adapter.isInitialized) {
                adapter.refresh()
            }
        }
    }

    private fun observeSharedListRefresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                friendsViewModel.refreshLists.collectLatest {
                    adapter.refresh()
                }
            }
        }
    }

    private fun updateFansSummary(count: Int) {
        val full = getString(R.string.friend_summary_fans, count)
        val number = count.toString()
        val start = full.indexOf(number)
        if (start < 0) {
            binding.tvSummary.text = full
            return
        }
        val spannable = SpannableString(full)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#212121")),
            start,
            start + number.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        binding.tvSummary.text = spannable
    }

    private fun showFanMoreBottomSheet(user: UserFriend) {
        val sheetBinding = DialogFanMoreBinding.inflate(layoutInflater)
        val dialog = BilibiliBottomSheetDialog(requireContext())
        dialog.setContentView(sheetBinding.root)

        sheetBinding.rowRemoveFan.setOnClickListener {
            dialog.dismiss()
            showRemoveFanConfirmDialog(user)
        }
        sheetBinding.rowCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showRemoveFanConfirmDialog(user: UserFriend) {
        AlertDialog.Builder(requireContext(), R.style.PinkDialogTheme)
            .setTitle("移除粉丝")
            .setMessage("确定将 ${user.otherNickName} 从你的粉丝中移除吗？")
            .setPositiveButton("确定") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.removeFan(user.otherUserId)) {
                        adapter.refresh()
                    } else {
                        Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openUserProfile(userId: String) {
        if (userId.isEmpty()) return
        startActivity(
            Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra("user_id", userId)
            },
        )
    }

    private fun showFollowConfirmDialog(user: UserFriend) {
        FollowConfirmDialog.show(requireContext(), user.otherNickName, currentlyFocused = false) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (viewModel.followUser(user.otherUserId)) {
                    friendsViewModel.notifyFollowRelationChanged()
                } else {
                    Toast.makeText(context, "关注失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCancelFollowDialog(user: UserFriend) {
        FollowConfirmDialog.show(requireContext(), user.otherNickName, currentlyFocused = true) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (viewModel.cancelFollow(user.otherUserId)) {
                    friendsViewModel.notifyFollowRelationChanged()
                } else {
                    Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.bilibili.ui.focus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.databinding.FragmentFocusOnBinding

class FocusOnFragment : Fragment() {
    private var _binding: FragmentFocusOnBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusOnViewModel by viewModels()
    private lateinit var focusAdapter: FocusOnAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFocusOnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化适配器
        focusAdapter = FocusOnAdapter(emptyList()) { user ->
            // 这里执行点击按钮后的逻辑：通常是弹出对话框确认是否取消关注
            viewModel.cancelFollow(user.otherUserId)
        }

        // 2. 设置 RecyclerView
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = focusAdapter

        // 3. 观察数据
        viewModel.friendList.observe(viewLifecycleOwner) { list ->
            focusAdapter.updateData(list)
            // 更新顶部人数显示
            binding.tvCountNumber.text = "${list.size}人"
        }

        // 4. 加载数据
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
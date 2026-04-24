package com.example.bilibili.ui.personal.fans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.databinding.FragmentFansBinding

class FansFragment : Fragment() {
    private var _binding: FragmentFansBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FansViewModel by viewModels()
    private lateinit var fansAdapter: FansAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化适配器
        fansAdapter = FansAdapter(emptyList()) { user ->
            // 这里执行点击按钮后的逻辑：通常是弹出对话框确认是否回关
            viewModel.followBack(user.otherUserId)
        }

        // 2. 设置 RecyclerView
        binding.rvFans.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFans.adapter = fansAdapter

        // 3. 观察数据
        viewModel.fanList.observe(viewLifecycleOwner) { list ->
            fansAdapter.updateData(list)
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

    companion object {
        @JvmStatic
        fun newInstance() = FansFragment()
    }
}
package com.example.bilibili.ui.personal.contribute

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.example.bilibili.data.api.PostService
import com.example.bilibili.databinding.FragmentContributeBinding
import com.example.bilibili.ui.playVideo.PlayVideoActivity
import com.example.bilibili.util.RetrofitClient

class ContributeFragment : Fragment() {
    private var _binding: FragmentContributeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContributeViewModel
    private lateinit var adapter: ContributeVideoAdapter

    // 记录当前的排序类型，默认为 0 (最新发布)
    private var currentOrderType = 0
    private val userId = "5952889127"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContributeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(
            this,
            ContributeViewModel.ContributeViewModelFactory(RetrofitClient.create(PostService::class.java))
        )[ContributeViewModel::class.java]

        setupRecyclerView()
        setupSortClick() // 设置点击切换逻辑
        observeData()

        // 初始加载
        viewModel.loadContributeVideos(userId, currentOrderType)
    }

    private fun setupRecyclerView() {
        adapter = ContributeVideoAdapter { video ->
            val intent = Intent(requireContext(), PlayVideoActivity::class.java).apply {
                putExtra("video_id", video.videoId)
            }
            startActivity(intent)
        }
        binding.rvVideoList.adapter = adapter
    }

    /**
     * 设置排序按钮的点击逻辑
     */
    private fun setupSortClick() {
        binding.New.setOnClickListener { view ->
            // 弹出菜单供用户选择，比循环切换体验更好
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add(0, 0, 0, "最新发布")
            popup.menu.add(0, 1, 1, "最多播放")
            popup.menu.add(0, 2, 2, "最多收藏")

            popup.setOnMenuItemClickListener { item ->
                // 1. 更新当前排序类型
                currentOrderType = item.itemId

                // 2. 更新 UI 文字
                binding.NewText.text = item.title
                // 选中后给文字改个颜色（B站粉），增加反馈感
                binding.NewText.setTextColor(Color.parseColor("#FB7299"))

                // 3. 重新发起网络请求
                viewModel.loadContributeVideos(userId, currentOrderType)

                true
            }
            popup.show()
        }
    }

    private fun observeData() {
        viewModel.videoList.observe(viewLifecycleOwner) { list ->
            // 普通 RecyclerView 会直接替换旧列表
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ContributeFragment()
    }
}
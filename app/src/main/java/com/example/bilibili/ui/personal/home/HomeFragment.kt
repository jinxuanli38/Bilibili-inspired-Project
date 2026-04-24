package com.example.bilibili.ui.personal.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bilibili.data.api.PostService
import com.example.bilibili.databinding.FragmentHomeBinding
import com.example.bilibili.ui.playVideo.PlayVideoActivity
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.SPUtils
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoAdapter: HomeVideoAdapter
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // 先得到service实例
                val apiService = RetrofitClient.create(PostService::class.java)

                // 返回创建好的 ViewModel
                return HomeViewModel(apiService) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 Adapter，并实现点击回调
        videoAdapter = HomeVideoAdapter { video ->
            val intent = Intent(requireContext(), PlayVideoActivity::class.java).apply {
                putExtra("video_id", video.videoId)
            }
            startActivity(intent)
        }

        binding.rvHomeVideo.adapter = videoAdapter

        // 1. 观察数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videoList.collect { list ->
                videoAdapter.submitList(list)
            }
        }

        // 2. 主动触发一次加载
        viewModel.fetchHomeVideos(SPUtils.getUserId())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
package com.example.bilibili.ui.personal.collect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.databinding.FragmentCollectBinding
import com.example.bilibili.ui.playVideo.PlayVideoActivity
import com.example.bilibili.util.SPUtils

class CollectFragment : Fragment() {

    private var _binding: FragmentCollectBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CollectViewModel
    private lateinit var collectAdapter: CollectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CollectViewModel::class.java]

        setupRecyclerView()

        viewModel.collectVideos.observe(viewLifecycleOwner) { list ->
            collectAdapter.setData(list)
        }

        val testUserId = SPUtils.getUserId()
        viewModel.loadCollections(testUserId)
    }

    private fun setupRecyclerView() {
        // 初始化 Adapter，并实现点击回调
        collectAdapter = CollectAdapter { video ->
            val intent = Intent(requireContext(), PlayVideoActivity::class.java).apply {
                putExtra("video_id", video.videoId)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            // 关键：因为你的 item 是横向的长条布局，必须用 LinearLayoutManager
            layoutManager = LinearLayoutManager(requireContext())
            adapter = collectAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CollectFragment()
    }
}
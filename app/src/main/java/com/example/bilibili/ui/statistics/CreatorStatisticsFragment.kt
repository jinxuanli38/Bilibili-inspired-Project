package com.example.bilibili.ui.statistics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bilibili.R
import com.example.bilibili.data.model.CreatorStatistics
import com.example.bilibili.data.model.ServiceAction
import com.example.bilibili.databinding.FragmentCreatorStatisticsBinding
import com.example.bilibili.ui.creator.CreatorCommentManageActivity
import com.example.bilibili.ui.creator.CreatorDanmuManageActivity
import com.example.bilibili.ui.creator.CreatorVideoManageActivity
import com.example.bilibili.ui.login.LoginActivity
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.ToastUtils

class CreatorStatisticsFragment : Fragment() {

    private var _binding: FragmentCreatorStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatorStatisticsViewModel by viewModels()
    private val statAdapter = CreatorStatAdapter { item ->
        viewModel.loadChart(item.dataType)
    }
    private val serviceAdapter = CreatorServiceAdapter { entry ->
        when (entry.action) {
            ServiceAction.VIDEO_POST -> startActivity(
                Intent(requireContext(), CreatorVideoManageActivity::class.java),
            )
            ServiceAction.COMMENT -> startActivity(
                Intent(requireContext(), CreatorCommentManageActivity::class.java),
            )
            ServiceAction.DANMU -> startActivity(
                Intent(requireContext(), CreatorDanmuManageActivity::class.java),
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreatorStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupRecyclerViews()
        setupRefresh()

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                CreatorStatisticsViewModel.UiState.Loading -> showLoading()
                CreatorStatisticsViewModel.UiState.NeedLogin -> showNeedLogin()
                is CreatorStatisticsViewModel.UiState.Error -> showError(state.message)
                is CreatorStatisticsViewModel.UiState.Success -> showContent(state.data)
            }
        }

        viewModel.chart.observe(viewLifecycleOwner) { chart ->
            binding.cardChart.tvChartTitle.text = chart.title
            binding.cardChart.chartView.setData(chart.points, chart.metricLabel)
            val allZero = chart.points.isNotEmpty() && chart.points.all { it.value == 0 }
            binding.cardChart.tvChartHint.isVisible = allZero && !chart.loadFailed
            if (chart.loadFailed) {
                ToastUtils.showShort(requireContext(), getString(R.string.creator_chart_load_failed))
            }
        }

        viewModel.selectedDataType.observe(viewLifecycleOwner) { type ->
            statAdapter.setSelectedDataType(type)
        }

        viewModel.refresh()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollContent) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.creator_stat_title_padding_extra)
            v.updatePadding(top = top + extra)
            insets
        }
    }

    private fun setupRecyclerViews() {
        val statGrid = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (position == 0) 2 else 1
            }
        }
        binding.rvStats.layoutManager = statGrid
        binding.rvStats.adapter = statAdapter
        binding.rvStats.itemAnimator = null

        binding.rvServices.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvServices.adapter = serviceAdapter
        binding.rvServices.itemAnimator = null
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.bili_pink)
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
    }

    private fun showLoading() {
        binding.progress.isVisible = true
        binding.tvEmpty.isVisible = false
        binding.cardProfile.isVisible = false
        binding.tvSectionTitle.isVisible = false
        binding.rvStats.isVisible = false
        binding.cardChart.root.isVisible = false
        binding.tvServicesTitle.isVisible = false
        binding.rvServices.isVisible = false
    }

    private fun showNeedLogin() {
        binding.progress.isVisible = false
        binding.cardProfile.isVisible = false
        binding.tvSectionTitle.isVisible = false
        binding.rvStats.isVisible = false
        binding.cardChart.root.isVisible = false
        binding.tvServicesTitle.isVisible = false
        binding.rvServices.isVisible = false
        binding.tvEmpty.isVisible = true
        binding.tvEmpty.text = getString(R.string.creator_statistics_need_login)
        binding.tvEmpty.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    private fun showError(message: String) {
        binding.progress.isVisible = false
        binding.cardProfile.isVisible = false
        binding.tvSectionTitle.isVisible = false
        binding.rvStats.isVisible = false
        binding.cardChart.root.isVisible = false
        binding.tvServicesTitle.isVisible = false
        binding.rvServices.isVisible = false
        binding.tvEmpty.isVisible = true
        binding.tvEmpty.text = message
        binding.tvEmpty.setOnClickListener { viewModel.refresh() }
    }

    private fun showContent(data: CreatorStatistics) {
        binding.progress.isVisible = false
        binding.tvEmpty.isVisible = false
        binding.cardProfile.isVisible = true
        binding.tvSectionTitle.isVisible = true
        binding.rvStats.isVisible = true
        binding.cardChart.root.isVisible = true
        binding.tvServicesTitle.isVisible = true
        binding.rvServices.isVisible = true

        binding.tvNickname.text = data.nickname
        GlideEngine.loadUserAvatar(requireContext(), data.avatar, binding.ivAvatar)
        statAdapter.submitList(data.stats)
        serviceAdapter.submitList(data.services)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

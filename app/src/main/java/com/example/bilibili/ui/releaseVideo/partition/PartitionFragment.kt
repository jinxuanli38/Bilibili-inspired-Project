package com.example.bilibili.ui.releaseVideo.partition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bilibili.data.api.CategoryInfoService
import com.example.bilibili.data.model.CategoryInfo
import com.example.bilibili.databinding.FragmentPartitionBinding
import com.example.bilibili.ui.releaseVideo.ReleaseVideoViewModel
import com.example.bilibili.util.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class PartitionFragment : Fragment() {

    private var _binding: FragmentPartitionBinding? = null
    private val binding get() = _binding!!

    // 使用 activityViewModels 与 MainTagFragment 共享同一个 ViewModel 实例
    private val viewModel: ReleaseVideoViewModel by activityViewModels()

    // 分区数据源（可以从 strings.xml 获取或由 ViewModel 提供）
    private val partitionList = mutableListOf<CategoryInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("创建成果",",")
        // 发起请求获取分区数据
        val service = RetrofitClient.create(CategoryInfoService::class.java)

        // 1. 初始化 Adapter
        val partitionAdapter = PartitionAdapter { name ->
            // 点击逻辑：更新数据并滑回主页
            viewModel.updatePartition(name)
        }

        // 2. 配置 RecyclerView
        binding.rvPartition.apply {
            layoutManager = GridLayoutManager(requireContext(), 4) // 4列网格
            adapter = partitionAdapter
            setHasFixedSize(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val categoryInfo = JSONObject(service.loadAllCategoryInfo())

            if (categoryInfo.optInt("code") == 200) {
                val data = categoryInfo.getJSONArray("data")
                for (i in 0 until data.length() ) {
                    val item = data.getJSONObject(i)
                    val categoryInfo = CategoryInfo(
                        categoryId = item.optInt("categoryId"),
                        categoryName = item.getString("categoryName")
                    )
                    partitionList.add(categoryInfo)
                }
            }

            // 确保获取完请求再初始化适配器
            partitionAdapter.submitList(partitionList)

        }

        // 4. 观察 ViewModel 里的选中分区，实时更新 Adapter 的选中态
        viewModel.selectedPartition.observe(viewLifecycleOwner) { selectedCategoryInfo ->
            partitionAdapter.setSelected(selectedCategoryInfo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
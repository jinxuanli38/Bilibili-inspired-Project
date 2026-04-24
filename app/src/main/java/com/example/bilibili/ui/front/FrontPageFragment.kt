package com.example.bilibili.ui.front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.data.api.CategoryInfoService
import com.example.bilibili.data.api.VideoService
import com.example.bilibili.data.model.CategoryItem
import com.example.bilibili.data.model.VideoItem
import com.example.bilibili.databinding.FragmentFrontPageBinding
import com.example.bilibili.util.RetrofitClient
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FrontPageFragment : Fragment() {
    private var _binding: FragmentFrontPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoAdapter: VideoAdapter

    // --- 新增：分页与状态变量 ---
    private var currentPage = 1
    private var currentCategoryId = 0
    private var isLoadingMore = false // 标记是否正在加载更多，防止重复请求

    private val videoService = RetrofitClient.create(VideoService::class.java)
    private val categoryService = RetrofitClient.create(CategoryInfoService::class.java)

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFrontPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 RecyclerView (注意：Adapter需要支持追加数据)
        videoAdapter = VideoAdapter(mutableListOf())
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = videoAdapter

            // --- 新增：上拉加载监听 ---
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val lm = layoutManager as GridLayoutManager
                    val totalItemCount = lm.itemCount
                    val lastVisibleItem = lm.findLastVisibleItemPosition()

                    // 当滑动到倒数第 2 个条目且不在加载中时，触发加载更多
                    if (dy > 0 && !isLoadingMore && lastVisibleItem >= totalItemCount - 2) {
                        loadMoreData()
                    }
                }
            })
        }

        // 2. --- 新增：下拉刷新监听 ---
        binding.swipeRefreshLayout.setColorSchemeColors(android.graphics.Color.parseColor("#FB7299")) // B站粉
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        // 3. 加载分类
        loadTabs()
    }

    private fun loadTabs() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = withContext(Dispatchers.IO) { categoryService.loadAllCategoryInfo() }
                val categories = parseCategoryJson(response)

                categories.forEach { category ->
                    val tab = binding.tabLayout.newTab().apply {
                        text = category.categoryName
                        tag = category.categoryId
                    }
                    binding.tabLayout.addTab(tab)
                }

                binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        currentCategoryId = tab?.tag as? Int ?: 0
                        refreshData() // 切换分类视为一次刷新
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) { refreshData() }
                })

                if (categories.isNotEmpty()) {
                    currentCategoryId = categories[0].categoryId
                    refreshData()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- 核心逻辑：下拉刷新 ---
    private fun refreshData() {
        currentPage = 1
        fetchVideoList(isLoadMore = false)
    }

    // --- 核心逻辑：上拉加载 ---
    private fun loadMoreData() {
        isLoadingMore = true
        currentPage++
        fetchVideoList(isLoadMore = true)
    }

    private fun fetchVideoList(isLoadMore: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // 如果不是刷新，且没有正在显示刷新进度条，可以手动显示（可选）
                // if (!isLoadMore) binding.swipeRefreshLayout.isRefreshing = true

                val response = withContext(Dispatchers.IO) {
                    videoService.loadVideo(pageNo = currentPage, pCategoryId = currentCategoryId)
                }
                val newVideos = parseVideoJson(response)

                if (isLoadMore) {
                    // 上拉加载：向 Adapter 追加数据
                    videoAdapter.addData(newVideos)
                    isLoadingMore = false
                } else {
                    // 下拉刷新：重置 Adapter 数据
                    videoAdapter.updateData(newVideos)
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                // --- 切换空状态显隐 ---
                val isEmpty = videoAdapter.itemCount == 0
                binding.llEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
                binding.swipeRefreshLayout.isRefreshing = false
                isLoadingMore = false
            }
        }
    }

    // 手动解析分类 JSON
    private fun parseCategoryJson(json: String): List<CategoryItem> {
        val list = mutableListOf<CategoryItem>()
        val dataArray = JSONObject(json).getJSONArray("data")
        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            list.add(CategoryItem(
                categoryId = item.getInt("categoryId"),
                categoryName = item.getString("categoryName")
            ))
        }
        return list
    }

    private fun parseVideoJson(json: String): List<VideoItem> {
        val list = mutableListOf<VideoItem>()
        try {
            val root = JSONObject(json)
            // 先判断状态码是否成功
            if (root.optInt("code") == 200) {
                val dataArray = root.getJSONObject("data").optJSONArray("list")
                if (dataArray != null) {
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)

                        // 对应你 VideoItem 的构造函数
                        val video = VideoItem(
                            videoId = item.optString("videoId"),
                            videoName = item.optString("videoName"),
                            videoCover = item.optString("videoCover"),
                            playCount = item.optInt("playCount", 0),
                            commentCount = item.optInt("commentCount", 0),
                            duration = item.optInt("duration", 0),
                            createTime = item.optString("createTime"),
                            danmuCount = item.optInt("danmuCount", 0),
                            nickName = item.optString("nickName")
                        )

                        /* * 注意：如果你的 VideoItem 里面有 nickName 字段，
                         * 而 JSON 里的字段名可能是 "nickName" 或 "userName"，
                         * 请确保这里解析的 Key 与后端返回的一致。
                         */
                        // 如果 VideoItem 包含 nickName，请取消下面这行的注释或将其加入构造
                        // video.nickName = item.optString("nickName", "未知UP主")

                        list.add(video)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
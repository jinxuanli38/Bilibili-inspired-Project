package com.example.bilibili.ui.playVideo

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bilibili.MainActivity
import com.example.bilibili.R
import com.example.bilibili.data.model.DanmuEntity
import com.example.bilibili.databinding.ActivityPlayVideoBinding
import com.example.bilibili.databinding.DialogDanmuSettingBinding
import com.example.bilibili.ui.playVideo.comment.VideoCommentFragment
import com.example.bilibili.ui.playVideo.danmu.DanmuColorAdapter
import com.example.bilibili.ui.playVideo.intro.VideoIntroFragment
import com.example.bilibili.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shuyu.gsyvideoplayer.GSYVideoManager.releaseAllVideos
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView

class PlayVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayVideoBinding

    private val viewModel: PlayVideoViewModel by viewModels()

    private lateinit var orientationUtils: OrientationUtils

    private lateinit var currentFileId: String

    private lateinit var currentVideoId: String

    private var currentVideoUrl: String? = null

    private var currentVideoTitle: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 基础 UI 配置
        enableEdgeToEdge()
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ivGoHome = binding.videoPlayer.findViewById<ImageView>(R.id.iv_go_home)
        ivGoHome?.setOnClickListener {
            // 彻底停掉正在放的视频和音频，释放底层内核
            releaseAllVideos()

            // 跳转到主页 (把 MainActivity 换成你项目里实际的主页 Activity)
            val intent = Intent(this, MainActivity::class.java).apply {
                // 关键：利用这两个 Flag 清空当前所有视频历史页面栈，让主页处于最顶层
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)

            // 关掉当前的播放页面
            finish()
        }

        // 2. 初始化各组件
        initSystemUI()
        initViewPager()

        // 3. 注册数据观察者 (在请求数据前先订阅)
        initObservers()

        // 4. 获取 Intent 数据并触发加载
        val videoId = intent.getStringExtra("video_id") ?: ""
        if (videoId.isNotEmpty()) {
            // 调用 ViewModel 里的全量加载函数
            viewModel.fetchAllData(videoId)
        } else {
            ToastUtils.showShort(this, "视频 ID 缺失")
        }

        // 返回按钮逻辑
        binding.ivStickyBack.setOnClickListener { finish() }

        // 发送弹幕
        binding.danmu.setOnClickListener {
            danmuSheetDialog()
        }

        // 初始化方向工具类
        orientationUtils = OrientationUtils(this, binding.videoPlayer)
        // 设置全屏按钮是否跟随旋转
        orientationUtils.isEnable = true

        // 核心：给全屏按钮设置点击事件
        binding.videoPlayer.fullscreenButton.setOnClickListener {
            // 直接触发旋转并进入全屏模式
            orientationUtils.resolveByClick()
            // 开启全屏窗口
            binding.videoPlayer.startWindowFullscreen(this, true, true)
        }
    }

    private fun danmuSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentBottomSheetStyle)
        val dialogBinding = DialogDanmuSettingBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        val colorAdapter = DanmuColorAdapter { color ->
            // 暂时不需要预览
        }

        dialogBinding.rvDanmuColors.apply {
            layoutManager = GridLayoutManager(this@PlayVideoActivity, 5) // 每行5个
            adapter = colorAdapter
        }

        colorAdapter.submitList(viewModel.danmuColors)

        // 弹幕位置按钮逻辑 (你已经写的)
        val posButtons =
            listOf(dialogBinding.btnPosScroll, dialogBinding.btnPosTop, dialogBinding.btnPosBottom)
        posButtons.forEach { layout ->
            layout.setOnClickListener {
                posButtons.forEach { it.isSelected = false }
                layout.isSelected = true
            }
        }
        posButtons[0].isSelected = true

        // 发送按钮
        dialogBinding.ivDanmuSend.setOnClickListener {
            val content = dialogBinding.etDanmuMessage.text.toString().trim()
            if (content.isEmpty()) {
                ToastUtils.showShort(this, "请输入弹幕内容")
                return@setOnClickListener
            }

            // 构造弹幕对象
            val entity = DanmuEntity().apply {
                this.text = content
                this.color = colorAdapter.getSelectedColor()
                this.time = (binding.videoPlayer.currentPositionWhenPlaying / 1000).toInt()
                this.mode = when {
                    dialogBinding.btnPosTop.isSelected -> 1
                    dialogBinding.btnPosBottom.isSelected -> 2
                    else -> 0
                }
                this.videoId = currentVideoId
                this.fileId = currentFileId
            }

            // 需要喂给播放器
            binding.videoPlayer.addDanmakuEntity(entity)

            // 发送到服务端进行存储
            viewModel.sendDanmu(entity)

            // 隐藏键盘
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(dialogBinding.etDanmuMessage.windowToken, 0)

            bottomSheetDialog.dismiss()
        }

        // 延迟200ms执行
        dialogBinding.etDanmuMessage.postDelayed({
            // 1. 获取焦点
            dialogBinding.etDanmuMessage.requestFocus()
            // 2. 弹出键盘
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(dialogBinding.etDanmuMessage, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        bottomSheetDialog.show()
    }


    /**
     * 专门负责观察 ViewModel 中的 LiveData 数据变化
     */
    private fun initObservers() {
        // 观察播放地址：一旦拿到 M3U8 地址，立刻初始化 GSYPlayer
        viewModel.videoUrlLive.observe(this) { url ->
            if (!url.isNullOrEmpty()) {
                currentVideoUrl = url
                currentVideoTitle = "视频播放"
                initPlayer(url, "视频播放")
            }
        }

        // 观察视频详情
        viewModel.videoDetailLive.observe(this) { videoInfo ->
            currentVideoId = videoInfo.optString("videoId")
        }

        // 观察文件ID
        viewModel.fileIdLive.observe(this) { fileId ->
            currentFileId = fileId
        }

        // 观察错误信息
        viewModel.errorLive.observe(this) { errorMsg ->
            ToastUtils.showShort(this, errorMsg)
        }

        // 观察弹幕数据
        viewModel.danmuListLive.observe(this) { danmuEntities ->
            // 否则新视频一开播，旧视频的弹幕还在引擎里飘，时钟直接对撞卡死！
            binding.videoPlayer.danmakuView?.removeAllDanmakus(true)

            // 发送所有加载的弹幕
            danmuEntities.forEach { entity ->
                binding.videoPlayer.addDanmakuEntity(entity)
            }
        }
    }

    // 当用户主动按 Home 键或划出 App 返回桌面时触发
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        // 如果视频正在播放，且系统版本支持（Android 8.0+），就直接切入画中画小窗
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isPlaying = binding.videoPlayer.currentPlayer.currentState == GSYVideoView.CURRENT_STATE_PLAYING
            if (isPlaying) {
                val params = PictureInPictureParams.Builder().build()
                enterPictureInPictureMode(params)
            }
        }
    }

    // 当画中画小窗状态发生改变时回调
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            // 1. 已经进入桌面小窗：隐藏顶部和底部控制栏
            binding.videoPlayer.findViewById<View>(R.id.layout_top)?.visibility = View.GONE
            binding.videoPlayer.findViewById<View>(R.id.layout_bottom)?.visibility = View.GONE
            binding.videoPlayer.findViewById<View>(R.id.bottom_progressbar)?.visibility = View.GONE

            // 2. 如果你有弹幕，小窗里也必须关掉，否则密密麻麻卡死
            binding.videoPlayer.danmakuView?.hide()
        } else {
            // 3. 用户从小窗点击恢复，回到了主 App：把控制栏和弹幕重新变回来
            binding.videoPlayer.findViewById<View>(R.id.layout_top)?.visibility = View.VISIBLE
            binding.videoPlayer.findViewById<View>(R.id.layout_bottom)?.visibility = View.VISIBLE
            binding.videoPlayer.findViewById<View>(R.id.bottom_progressbar)?.visibility = View.VISIBLE

            binding.videoPlayer.danmakuView?.show()
        }
    }

    /**
     * 系统级 UI 适配：处理刘海屏和状态栏占位
     */
    private fun initSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        // 刘海屏/水滴屏适配：给播放器容器添加状态栏高度的 padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.playerContainer) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.playerContainer.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
    }

    /**
     * 设置 ViewPager2 (简介与评论切换)
     */
    private fun initViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) VideoIntroFragment() else VideoCommentFragment()
            }
        }

        // 绑定 TabLayout 与 ViewPager2
        TabLayoutMediator(binding.videoTabLayout, binding.viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "简介"
            } else {
                // 观察评论总数变化
                viewModel.commentTotalCount.observe(this@PlayVideoActivity) { totalCount ->
                    if (tab.text.toString().startsWith("评论")) {
                        tab.text = "评论 $totalCount"
                    }
                }
                // 初始设置
                val currentCount = viewModel.commentTotalCount.value ?: 0
                tab.text = "评论 $currentCount"
            }
        }.attach()

        // 监听 Tab 切换事件
        binding.videoTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    // 切换到评论Tab时，让底部栏立即可见
                    binding.viewPager.postDelayed({
                        binding.viewPager.requestLayout()
                    }, 100)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * 初始化 GSYVideoPlayer
     */
    private fun initPlayer(url: String, title: String) {
        binding.videoPlayer.onVideoReset()

        binding.videoPlayer.setUp(url, true, null)
        binding.videoPlayer.setIsTouchWiget(true)

        // 隐藏自带的返回键
        binding.videoPlayer.backButton.visibility = View.GONE

        binding.videoPlayer.post {
            binding.videoPlayer.startPlayLogic()
        }
    }

    override fun onPause() {
        super.onPause()
        // 进入了桌面画中画模式，视频不暂停继续播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            return
        }

        if (binding.videoPlayer.currentPlayer != null) {
            binding.videoPlayer.onVideoPause()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从小窗回到了大窗
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            if (binding.videoPlayer.currentPlayer != null) {
                binding.videoPlayer.onVideoResume()
            }
            return
        }

        try {
            // 检查播放器是否可用
            if (binding.videoPlayer.currentPlayer != null) {
                // 播放器正常，恢复播放
                binding.videoPlayer.onVideoResume()
            } else {
                // 播放器已被释放或未初始化，重新初始化
                if (!currentVideoUrl.isNullOrEmpty()) {
                    currentVideoUrl?.let { url ->
                        currentVideoTitle?.let { title ->
                            initPlayer(url, title)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 发生异常时尝试重新初始化
            if (!currentVideoUrl.isNullOrEmpty()) {
                currentVideoUrl?.let { url ->
                    currentVideoTitle?.let { title ->
                        initPlayer(url, title)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 彻底释放屏幕旋转工具类，防止严重的内存泄漏和传感器死锁
        if (::orientationUtils.isInitialized) {
            orientationUtils.releaseListener()
        }

        // 只释放当前播放器，不影响其他Activity的播放器
        try {
            binding.videoPlayer.release()
        } catch (e: Exception) {
            // 忽略释放异常
        }
    }
}
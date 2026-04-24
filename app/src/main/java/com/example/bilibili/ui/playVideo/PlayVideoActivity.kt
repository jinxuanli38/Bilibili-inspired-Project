package com.example.bilibili.ui.playVideo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.bilibili.R
import com.example.bilibili.data.api.PostService
import com.example.bilibili.data.api.UserActionService
import com.example.bilibili.data.api.VideoService
import com.example.bilibili.databinding.ActivityPlayVideoBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PlayVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayVideoBinding
    private var player: ExoPlayer? = null
    private val videoService = RetrofitClient.create(VideoService::class.java)
    private val postService = RetrofitClient.create(PostService::class.java)
    private var isFollowed = false
    private var currentUserId: String = "" // 视频作者的 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 获取 Intent 传递过来的 videoId
        val videoId = intent.getStringExtra("video_id") ?: ""

        if (videoId.isNotEmpty()) {
            fetchVideoDetail(videoId)
            // 初始化交互按钮
            initActionButtons(videoId)
        } else {
            Log.e("PlayVideo", "VideoID 缺失")
        }
    }

    private fun fetchVideoDetail(videoId: String) {
        lifecycleScope.launch {
            try {
                // 调用接口获取数据
                val (response, pList) = withContext(Dispatchers.IO) {
                    // 分别获取两个请求的结果
                    val info = videoService.getVideoInfo(videoId)
                    val pList = videoService.loadVideoPList(videoId)
                    Pair(info, pList)
                }

                val root = JSONObject(response)
                if (root.optInt("code") == 200) {
                    val data = root.getJSONObject("data")
                    val videoInfo = data.getJSONObject("videoInfo")

                    // 获取对应的up主信息
                    val response = withContext(Dispatchers.IO) {
                        postService.getUserInfo(videoInfo.getString("userId"))
                    }

                    // 3. 渲染页面数据
                    bindVideoInfo(videoInfo, JSONObject(response).getJSONObject("data"))

                    // 准备播放视频
                    val dataArray = JSONObject(pList).getJSONArray("data")
                    // 2. 拿到数组里的第一个对象，并强制转换为 JSONObject
                    if (dataArray.length() > 0) {
                        val fileObj = dataArray.getJSONObject(0) // 必须用 getJSONObject 而不是 get()

                        // 3. 开始提取具体字段
                        val fileId = fileObj.optString("fileId")
                        val userId = fileObj.optString("userId")
                        val videoId = fileObj.optString("videoId")
                        val filePath = fileObj.optString("filePath")
                        val duration = fileObj.optInt("duration")

                        val videoUrl =
                            "${RetrofitClient.BASE_URL}file/videoResource/$fileId/"
                        initPlayer(videoUrl)

                        // 打印调试一下
                        Log.d("VideoSource", "获取到 fileId: $fileId, 路径: $filePath")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindVideoInfo(info: JSONObject, user: JSONObject) {
        binding.apply {
            currentUserId = info.getString("userId")
            // 基础信息
            tvVideoTitle.text = info.optString("videoName")
            tvPostTime.text = formatPublishTime(info.optString("createTime"))

            // 关注按钮
            updateFollowStatusUI(user.getBoolean("haveFocus"))
            // 关注/取关
            btnFollow.setOnClickListener {
                handleFollowAction()
            }

            // 播放量与弹幕
            tvPlayCount.text = formatCount(info.optInt("playCount"))
            tvDanmakuCount.text = info.optString("danmuCount")

            // 加载头像
            val avatarUrl = user.optString("avatar")
            GlideEngine.loadVideoCover(this@PlayVideoActivity, avatarUrl, ivAvatar)

            // 记载用户信息
            tvAuthorName.text = user.getString("nickName")
            tvFansCount.text = "${user.getString("fansCount")}粉丝"

            // 点赞、投币、收藏 (根据你的 XML 结构查找子 TextView)
            llLikeCount.text = info.optString("likeCount")
            llCoinCount.text = info.optString("coinCount")
            llFavCount.text = info.optString("collectCount")
        }
    }

    private fun initPlayer(url: String) {
        // 检查并释放旧的播放器（防止内存泄漏）
        if (player == null) {
            player = ExoPlayer.Builder(this).build()
            binding.playerView.player = player
        }

        // 明确指定 HLS 格式
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8) // 强制指定为 HLS
            .build()

        // 设置媒体源并准备
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // 准备好了自动播放
        }
    }

    // 格式化万级单位
    private fun formatCount(count: Int): String {
        return if (count >= 10000) String.format("%.1f万", count / 10000.0) else count.toString()
    }

    // --- 生命周期管理 ---
    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    private fun formatPublishTime(rawDate: String): String {
        return try {
            // 1. 定义原始格式 (匹配你后端返回的字符串)
            val inputFormat =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

            // 2. 定义目标格式 (你想要的格式)
            val outputFormat =
                java.text.SimpleDateFormat("yyyy年M月d日 HH:mm", java.util.Locale.getDefault())

            // 3. 执行转换
            val date = inputFormat.parse(rawDate)
            if (date != null) {
                outputFormat.format(date)
            } else {
                rawDate
            }
        } catch (e: Exception) {
            e.printStackTrace()
            rawDate // 如果解析出错（比如后端数据格式变了），至少保证不崩溃，返回原串
        }
    }

    private fun updateFollowStatusUI(followed: Boolean) {
        if (followed) {
            // --- 样式：已关注 (对应你的 image_1.png) ---
            binding.btnFollow.text = "已关注"
            // 灰色文字
            binding.btnFollow.setTextColor(android.graphics.Color.parseColor("#999999"))
            // 灰色全圆角背景
            binding.btnFollow.setBackgroundResource(R.drawable.bg_followed_button)
        } else {
            // --- 样式：未关注 ---
            binding.btnFollow.text = "+ 关注"
            // 白色文字
            binding.btnFollow.setTextColor(android.graphics.Color.WHITE)
            // 粉色全圆角背景 (你之前写的那个)
            binding.btnFollow.setBackgroundResource(R.drawable.bg_follow_button)
        }
    }

    private fun handleFollowAction() {
        // 1. 立即切换 UI 状态（乐观更新，让用户感觉很快）
        isFollowed = !isFollowed
        updateFollowStatusUI(isFollowed)

        // 2. 发起网络请求
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (isFollowed) {
                        // 执行关注
                        postService.focus(currentUserId)
                    } else {
                        // 执行取关
                        postService.cancelFocus(currentUserId)
                    }
                }
                // 因为后端没有具体返回内容，只要没进 catch 就算成功
                Log.d("Follow", "操作成功")
            } catch (e: Exception) {
                // 如果请求失败（比如没网），回滚 UI 状态并提示用户
                isFollowed = !isFollowed
                updateFollowStatusUI(isFollowed)
                ToastUtils.showShort(this@PlayVideoActivity, "操作失败，请重试")
                e.printStackTrace()
            }
        }
    }

    private fun initActionButtons(videoId: String) {
        // 点赞 (ActionType 0)
        binding.llLike.setOnClickListener {
            toggleAction(binding.llLikeIcon, binding.llLikeCount, 0, videoId)
        }

        // 投币 (ActionType 4)
        binding.llCoin.setOnClickListener {
            // 投币通常直接变亮，不一定支持反向切换，这里根据你的需求定
            toggleAction(binding.llCoinIcon, binding.llCoinCount, 4, videoId, count = 1)
        }

        // 收藏 (ActionType 2)
        binding.llFav.setOnClickListener {
            toggleAction(binding.llFavIcon, binding.llFavCount, 2, videoId)
        }
    }

    /**
     * 通用的状态切换函数
     */
    @SuppressLint("SetTextI18n")
    private fun toggleAction(
        icon: View,
        text: TextView,
        type: Int,
        videoId: String,
        count: Int? = null
    ) {
        // 1. 获取当前状态并取反
        val newState = !icon.isSelected

        icon.isSelected = newState
        text.isSelected = newState

        val currentNum = text.text.toString().toIntOrNull() ?: 0
        if (newState) {
            text.text = (currentNum + 1).toString()
        } else {
            text.text = (currentNum - 1).coerceAtLeast(0).toString()
        }

        // 4. 发起网络请求
        lifecycleScope.launch {
            try {
                RetrofitClient.create(UserActionService::class.java).doAction(videoId, type, count)
            } catch (e: Exception) {
                // 请求失败则回滚 UI
                icon.isSelected = !newState
                text.isSelected = !newState
                ToastUtils.showShort(this@PlayVideoActivity, "操作失败")
            }
        }
    }

}
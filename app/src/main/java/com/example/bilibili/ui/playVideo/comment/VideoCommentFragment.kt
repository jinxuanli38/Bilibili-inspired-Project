package com.example.bilibili.ui.playVideo.comment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bilibili.R
import com.example.bilibili.data.api.FileService
import com.example.bilibili.data.model.CommentItem
import com.example.bilibili.databinding.DialogCommentInputBinding
import com.example.bilibili.databinding.FragmentVideoCommentBinding
import com.example.bilibili.ui.playVideo.PlayVideoViewModel
import com.example.bilibili.ui.user.UserProfileActivity
import com.example.bilibili.ui.playVideo.ImagePreviewActivity
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class VideoCommentFragment : Fragment() {
    private var _binding: FragmentVideoCommentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayVideoViewModel by activityViewModels()

    private val commentAdapter by lazy { CommentAdapter() }

    // 当前视频ID（用于发布评论）
    private var currentVideoId: String? = null

    // 当前回复的评论ID（null表示发表新评论）
    private var replyCommentId: Int? = null

    // 底部对话框
    private var commentBottomSheetDialog: BottomSheetDialog? = null

    // 选中的图片路径
    private var selectedImagePath: String? = null

    // 选中的图片URI
    private var selectedImageUri: Uri? = null

    // 上传的图片路径（服务器返回的路径）
    private var uploadedImagePath: String? = null

    private val fileService = RetrofitClient.create(FileService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        observeViewModel()
        setupBottomBar()

        // 从 Activity 获取 videoId
        viewModel.videoDetailLive.observe(viewLifecycleOwner) { videoInfo ->
            currentVideoId = videoInfo.optString("videoId")
        }
    }

    private fun initRecyclerView() {
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
            setHasFixedSize(true)
        }

        // 使用函数式编程设置回调
        setupAdapterCallbacks()
    }

    /**
     * 使用函数式编程设置 Adapter 回调
     */
    private fun setupAdapterCallbacks() {
        // 点击评论项（回复评论）
        commentAdapter.onCommentClick = { commentItem ->
            openCommentDialog(commentItem)
        }

        // 点赞回调
        commentAdapter.onLikeClick = { commentItem ->
            handleLikeAction(commentItem)
        }

        // 踩回调
        commentAdapter.onDislikeClick = { commentItem ->
            handleDislikeAction(commentItem)
        }

        // 头像点击回调（跳转到用户主页）
        commentAdapter.onAvatarClick = { userId ->
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        // 图片点击预览回调
        commentAdapter.onImageClick = { imageUrl ->
            val activity = requireActivity()
            if (activity is androidx.appcompat.app.AppCompatActivity) {
                ImagePreviewActivity.start(activity, imageUrl)
            }
        }
    }

    /**
     * 处理点赞操作
     */
    private fun handleLikeAction(commentItem: CommentItem) {
        val videoId = currentVideoId
        if (videoId.isNullOrEmpty()) {
            ToastUtils.showShort(requireContext(), "视频信息加载中，请稍后")
            return
        }

        // 乐观更新 UI
        val oldIsLiked = commentItem.isLiked
        commentItem.isLiked = !oldIsLiked

        // 互斥：点赞后取消踩
        if (commentItem.isLiked) {
            commentItem.isHated = false
        }

        // 更新 UI
        val position = commentAdapter.differ.currentList.indexOf(commentItem)
        if (position >= 0) {
            commentAdapter.notifyItemChanged(position)
        }

        // 调用 ViewModel 执行网络请求
        viewModel.doCommentAction(
            videoId = videoId,
            commentId = commentItem.commentId,
            actionType = 0 // 0-点赞
        ) { _, success ->
            if (!success) {
                // 失败回滚 UI
                commentItem.isLiked = oldIsLiked
                val rollbackPosition = commentAdapter.differ.currentList.indexOf(commentItem)
                if (rollbackPosition >= 0) {
                    commentAdapter.notifyItemChanged(rollbackPosition)
                }
            }
            // 成功后，评论列表会自动刷新（ViewModel 中的 fetchComments）
        }
    }

    /**
     * 处理踩操作
     */
    private fun handleDislikeAction(commentItem: CommentItem) {
        val videoId = currentVideoId
        if (videoId.isNullOrEmpty()) {
            ToastUtils.showShort(requireContext(), "视频信息加载中，请稍后")
            return
        }

        // 乐观更新 UI
        val oldIsHated = commentItem.isHated
        commentItem.isHated = !oldIsHated

        // 互斥：踩后取消点赞
        if (commentItem.isHated) {
            commentItem.isLiked = false
        }

        // 更新 UI
        val position = commentAdapter.differ.currentList.indexOf(commentItem)
        if (position >= 0) {
            commentAdapter.notifyItemChanged(position)
        }

        // 调用 ViewModel 执行网络请求
        viewModel.doCommentAction(
            videoId = videoId,
            commentId = commentItem.commentId,
            actionType = 1 // 1-踩
        ) { _, success ->
            if (!success) {
                // 失败回滚 UI
                commentItem.isHated = oldIsHated
                val rollbackPosition = commentAdapter.differ.currentList.indexOf(commentItem)
                if (rollbackPosition >= 0) {
                    commentAdapter.notifyItemChanged(rollbackPosition)
                }
            }
            // 成功后，评论列表会自动刷新（ViewModel 中的 fetchComments）
        }
    }

    private fun setupBottomBar() {
        // 点击输入框打开评论对话框
        binding.tvFakeInput.setOnClickListener {
            openCommentDialog(null)
        }
    }

    /**
     * 打开评论输入对话框
     * @param replyItem 回复的评论对象（null表示发表新评论）
     */
    private fun openCommentDialog(replyItem: CommentItem? = null) {
        // 如果对话框已显示，先关闭
        commentBottomSheetDialog?.dismiss()

        commentBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle).apply {
            // 设置窗口软输入模式，让软键盘推动内容
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            val dialogBinding = DialogCommentInputBinding.inflate(layoutInflater)
            setContentView(dialogBinding.root)

            // 获取 BottomSheet 的行为对象
            val bottomSheet = dialogBinding.root.parent as? View
            val behavior = BottomSheetBehavior.from(bottomSheet ?: return@apply)
            behavior.peekHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            // 允许键盘推起对话框
            behavior.isHideable = false

            // 设置状态栏透明，确保软键盘能够正确推动内容
            window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            // 设置回复标题提示
            dialogBinding.etCommentInput.hint = if (replyItem != null) {
                replyCommentId = replyItem.commentId
                "回复 @${replyItem.nickName}"
            } else {
                replyCommentId = null
                "尊重是评论打动人心的入场券"
            }

            // 字数统计和输入监听
            dialogBinding.etCommentInput.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val length = s?.length ?: 0
                    // 根据输入内容或选中图片动态调整发送按钮状态
                    dialogBinding.tvSend.isEnabled = length > 0 || selectedImageUri != null
                    dialogBinding.tvSend.alpha = if (length > 0 || selectedImageUri != null) 1f else 0.5f
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            // 图片按钮（选择图片）
            dialogBinding.ivImage.setOnClickListener {
                selectImage(dialogBinding)
            }

            // 移除图片按钮
            dialogBinding.ivRemoveImage.setOnClickListener {
                selectedImagePath = null
                selectedImageUri = null
                uploadedImagePath = null
                dialogBinding.selectedImageContainer.visibility = View.GONE
                dialogBinding.ivSelectedImage.setImageURI(null)
            }

            // 发送按钮
            dialogBinding.tvSend.setOnClickListener {
                val content = dialogBinding.etCommentInput.text.toString().trim()
                if (content.isEmpty() && uploadedImagePath == null) {
                    ToastUtils.showShort(requireContext(), "请输入评论内容或选择图片")
                    return@setOnClickListener
                }

                val videoId = currentVideoId
                if (videoId.isNullOrEmpty()) {
                    ToastUtils.showShort(requireContext(), "视频信息加载中，请稍后")
                    return@setOnClickListener
                }

                // 如果有选中的图片，先上传
                if (selectedImageUri != null && uploadedImagePath == null) {
                    // 显示加载提示
                    dialogBinding.tvSend.isEnabled = false
                    dialogBinding.tvSend.text = "上传中..."

                    // 上传图片
                    lifecycleScope.launch {
                        val success = uploadImage(selectedImageUri!!) { path ->
                            uploadedImagePath = path
                        }

                        if (success && uploadedImagePath != null) {
                            // 上传成功，发送评论
                            viewModel.postComment(videoId, content, replyCommentId, uploadedImagePath)

                            // 隐藏键盘和对话框
                            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(dialogBinding.etCommentInput.windowToken, 0)
                            dismiss()
                        } else {
                            // 上传失败
                            dialogBinding.tvSend.isEnabled = true
                            dialogBinding.tvSend.text = "发布"
                            ToastUtils.showShort(requireContext(), "图片上传失败")
                        }
                    }
                } else {
                    // 没有图片或已经上传过，直接发送评论
                    viewModel.postComment(videoId, content, replyCommentId, uploadedImagePath)

                    // 隐藏键盘和对话框
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(dialogBinding.etCommentInput.windowToken, 0)
                    dismiss()
                }
            }

            show()

            // 延迟弹出键盘并聚焦输入框
            dialogBinding.etCommentInput.postDelayed({
                dialogBinding.etCommentInput.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(dialogBinding.etCommentInput, InputMethodManager.SHOW_IMPLICIT)
            }, 150)
        }
    }

    private fun observeViewModel() {
        // 观察评论列表数据变化
        viewModel.commentListLive.observe(viewLifecycleOwner) { list ->
            commentAdapter.setData(list)
        }

        // 观察评论发布结果
        viewModel.postCommentResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastUtils.showShort(requireContext(), "评论发表成功")
                // 清空回复状态
                replyCommentId = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 关闭对话框并清除引用
        commentBottomSheetDialog?.dismiss()
        commentBottomSheetDialog = null
        _binding = null
    }

    /**
     * 选择图片
     */
    private fun selectImage(dialogBinding: DialogCommentInputBinding) {
        PictureSelector.create(requireContext())
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideImageEngine())
            .setMaxSelectNum(1) // 只能选择一张图片
            .setMinSelectNum(1)
            .setImageSpanCount(4)
            .setSelectedData(emptyList<LocalMedia>())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    // 获取选中的图片
                    if (result.isNotEmpty()) {
                        val media = result[0]
                        selectedImageUri = media.path?.let { Uri.fromFile(File(it)) }

                        // 显示选中的图片
                        selectedImageUri?.let { uri ->
                            dialogBinding.selectedImageContainer.visibility = View.VISIBLE
                            dialogBinding.ivSelectedImage.setImageURI(uri)

                            // 启用发送按钮
                            val content = dialogBinding.etCommentInput.text.toString()
                            dialogBinding.tvSend.isEnabled = content.isNotEmpty()
                            dialogBinding.tvSend.alpha = if (content.isNotEmpty()) 1f else 0.5f
                        }
                    }
                }

                override fun onCancel() {
                    // 用户取消选择
                }
            })
    }

    /**
     * 上传图片到服务器
     */
    private suspend fun uploadImage(uri: Uri, onPathReceived: (String?) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 将 URI 转换为 File
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val tempFile = File(requireContext().cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }

                // 创建请求体
                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

                // createThumbnail 参数
                val createThumbnailBody = "false".toRequestBody("text/plain".toMediaTypeOrNull())

                // 上传图片
                val result = fileService.postImage(filePart, createThumbnailBody)
                val jsonObject = JSONObject(result)

                if (jsonObject.optInt("code") == 200) {
                    val imagePath = jsonObject.optString("data")
                    onPathReceived(imagePath)
                    true
                } else {
                    val errorMsg = jsonObject.optString("message", "上传失败")
                    Log.e("VideoCommentFragment", "图片上传失败: $errorMsg")
                    onPathReceived(null)
                    false
                }
            } catch (e: Exception) {
                Log.e("VideoCommentFragment", "图片上传异常", e)
                onPathReceived(null)
                false
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(videoId: String) = VideoCommentFragment().apply {
            arguments = Bundle().apply {
                putString("videoId", videoId)
            }
        }
    }
}
package com.example.bilibili.ui.playVideo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bilibili.R
import com.example.bilibili.databinding.ActivityImagePreviewBinding

/**
 * 图片预览Activity
 * 用于预览评论中的大图
 */
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"

        fun start(activity: AppCompatActivity, imageUrl: String) {
            val intent = Intent(activity, ImagePreviewActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ?: ""

        // 背景点击关闭
        binding.root.setOnClickListener {
            finish()
        }

        // 图片点击关闭
        binding.ivImage.setOnClickListener {
            finish()
        }

        // 关闭按钮点击
        binding.ivClose.setOnClickListener {
            finish()
        }

        // 加载图片
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .into(binding.ivImage)
        } else {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
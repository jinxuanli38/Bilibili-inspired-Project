package com.example.bilibili

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bilibili.databinding.ActivityMainBinding
import com.example.bilibili.ui.front.FrontPageFragment
import com.example.bilibili.ui.message.MessageFragment
import com.example.bilibili.ui.personal.PersonalFragment
import com.example.bilibili.ui.releaseVideo.ReleaseVideoActivity
import com.example.bilibili.ui.statistics.CreatorStatisticsFragment
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.PermissionHelper
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG_HOME = "tag_home"
        const val TAG_STATISTICS = "tag_statistics"
        const val TAG_MESSAGE = "tag_message"
        const val TAG_MINE = "tag_mine"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            switchFragment(TAG_HOME)
        }

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(TAG_HOME)
                R.id.nav_statistics -> switchFragment(TAG_STATISTICS)
                R.id.nav_message -> switchFragment(TAG_MESSAGE)
                R.id.nav_mine -> switchFragment(TAG_MINE)
            }
            true
        }

        binding.fabAdd.setOnClickListener {
            PermissionHelper.requestPublishVideo(this) { startVideoPicker() }
        }
    }

    private fun startVideoPicker() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofVideo())
            .setImageEngine(GlideEngine)
            .setSelectionMode(SelectModeConfig.SINGLE)
            .isDisplayCamera(true)
            .setMaxSelectNum(1)
            .isPreviewVideo(false)
            .isPreviewImage(false)
            .isPreviewAudio(false)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val media = result?.getOrNull(0) ?: return
                    val videoPath = media.realPath
                    val duration = media.duration
                    Log.d("BiliSelect", "路径: $videoPath, 时长: ${duration / 1000}秒")
                    startActivity(
                        Intent(this@MainActivity, ReleaseVideoActivity::class.java).apply {
                            putExtra("video_path", videoPath)
                            putExtra("video_duration", duration)
                        },
                    )
                }

                override fun onCancel() {
                    Log.d("BiliSelect", "用户取消选择")
                }
            })
    }

    private fun switchFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment.isAdded) transaction.hide(fragment)
        }
        var target = supportFragmentManager.findFragmentByTag(tag)
        if (target == null) {
            target = when (tag) {
                TAG_HOME -> FrontPageFragment()
                TAG_STATISTICS -> CreatorStatisticsFragment()
                TAG_MESSAGE -> MessageFragment()
                TAG_MINE -> PersonalFragment()
                else -> FrontPageFragment()
            }
            transaction.add(R.id.nav_host_fragment, target, tag)
        } else {
            transaction.show(target)
        }
        transaction.commit()
    }

    fun switchToPersonalTab() {
        binding.bottomNavView.selectedItemId = R.id.nav_mine
        switchFragment(TAG_MINE)
    }

    fun switchToHomeTab() {
        binding.bottomNavView.selectedItemId = R.id.nav_home
        switchFragment(TAG_HOME)
    }

    fun switchToContributeTab() {
        binding.bottomNavView.selectedItemId = R.id.nav_mine
        switchFragment(TAG_MINE)
        binding.root.post {
            val fragment = supportFragmentManager.findFragmentByTag(TAG_MINE)
            if (fragment is PersonalFragment) {
                fragment.switchToContributeTab()
            }
        }
    }
}

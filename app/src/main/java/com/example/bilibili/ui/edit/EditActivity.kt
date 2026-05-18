package com.example.bilibili.ui.edit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bilibili.data.api.PostService
import com.example.bilibili.databinding.ActivityEditBinding
import com.example.bilibili.util.GlideEngine
import com.example.bilibili.util.RetrofitClient
import com.example.bilibili.util.SPUtils
import com.example.bilibili.util.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupBackButton()
        loadUserInfo()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                val responseString = withContext(Dispatchers.IO) {
                    val postService = RetrofitClient.create(PostService::class.java)
                    postService.getUserInfo(SPUtils.getUserId())
                }

                val userInfo = JSONObject(responseString)
                if (userInfo.optInt("code") == 200) {
                    val data = userInfo.getJSONObject("data")
                    bindUserData(data)
                } else {
                    ToastUtils.showShort(this@EditActivity, "获取用户信息失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtils.showShort(this@EditActivity, "网络请求失败")
            }
        }
    }

    private fun bindUserData(data: JSONObject) {
        // 头像
        val avatar = data.optString("avatar", "")
        SPUtils.saveAvatar(avatar) // 更新本地存储的头像
        GlideEngine.loadUserAvatar(this, avatar, binding.ivAvatar)

        // 昵称
        val nickName = data.optString("nickName", "")
        binding.tvNickname.text = nickName.ifEmpty { "未设置" }

        // 性别 (1=男，0=女，2=保密)
        val sex = data.optInt("sex", 2)
        val sexText = when (sex) {
            1 -> "男"
            0 -> "女"
            else -> "保密"
        }
        binding.tvGender.text = sexText

        // 生日
        val birthday = data.optString("birthday", "")
        binding.tvBirthday.text = birthday.ifEmpty { "未设置" }

        // 个性签名
        val signature = data.optString("personalIntroduction", "")
        binding.tvSignature.text = signature.ifEmpty { "未设置" }

        // 学校
        val school = data.optString("school", "")
        binding.tvSchool.text = school.ifEmpty { "未设置" }

        // UID
        val uid = data.optString("userId", "")
        binding.tvUid.text = uid
    }
}
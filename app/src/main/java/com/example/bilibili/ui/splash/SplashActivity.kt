package com.example.bilibili.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bilibili.MainActivity
import com.example.bilibili.databinding.ActivitySplashBinding
import com.example.bilibili.ui.login.LoginActivity
import com.example.bilibili.util.AuthHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 启动页：仅展示 login_bg 全屏封面，后台校验登录态后进入主页或登录页。
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val startMs = System.currentTimeMillis()
            val target = withContext(Dispatchers.IO) {
                AuthHelper.resolveLaunchTarget()
            }
            val remain = MIN_SPLASH_MS - (System.currentTimeMillis() - startMs)
            if (remain > 0) delay(remain)

            when (target) {
                is AuthHelper.LaunchTarget.Main ->
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                is AuthHelper.LaunchTarget.Login ->
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }

    companion object {
        private const val MIN_SPLASH_MS = 900L
    }
}

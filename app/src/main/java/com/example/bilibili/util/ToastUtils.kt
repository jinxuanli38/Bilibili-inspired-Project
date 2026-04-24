package com.example.bilibili.util

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var mToast: Toast? = null

    /**
     * 显示短 Toast
     * @param context 上下文
     * @param message 消息内容
     */
    fun showShort(context: Context, message: CharSequence) {
        show(context, message, Toast.LENGTH_SHORT)
    }

    /**
     * 显示长 Toast
     * @param context 上下文
     * @param message 消息内容
     */
    fun showLong(context: Context, message: CharSequence) {
        show(context, message, Toast.LENGTH_LONG)
    }

    private fun show(context: Context, message: CharSequence, duration: Int) {
        // 如果当前已经有一个 Toast 在显示，先取消它（或者直接改文字）
        // 这种做法保证了即使疯狂点击，也只会显示最后一次的内容，且不会堆叠
        mToast?.cancel()
        
        // 使用 applicationContext 避免内存泄漏
        mToast = Toast.makeText(context.applicationContext, message, duration)
        mToast?.show()
    }
}
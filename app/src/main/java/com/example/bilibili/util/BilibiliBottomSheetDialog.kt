package com.example.bilibili.util

import android.content.Context
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.annotation.StyleRes
import com.example.bilibili.R
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * BottomSheet 使用独立 Window，Activity 生命周期里的全局扫描覆盖不到。
 * 在 onStart 时对弹窗 decorView 内的 EditText 应用粉色文字选择器。
 */
class BilibiliBottomSheetDialog @JvmOverloads constructor(
    context: Context,
    @StyleRes theme: Int = R.style.TransparentBottomSheetStyle,
) : BottomSheetDialog(context, theme) {

    private var focusListener: ViewTreeObserver.OnGlobalFocusChangeListener? = null

    override fun onStart() {
        super.onStart()
        installPinkEditTextHandles()
    }

    override fun onStop() {
        focusListener?.let { listener ->
            window?.decorView?.viewTreeObserver?.removeOnGlobalFocusChangeListener(listener)
        }
        focusListener = null
        super.onStop()
    }

    private fun installPinkEditTextHandles() {
        val decor = window?.decorView ?: return
        decor.post {
            TextSelectHandleHelper.applyPinkHandlesIn(decor)
            if (focusListener == null) {
                focusListener = ViewTreeObserver.OnGlobalFocusChangeListener { _, newFocus ->
                    if (newFocus is EditText) {
                        TextSelectHandleHelper.applyPinkHandles(newFocus)
                    }
                }
                decor.viewTreeObserver.addOnGlobalFocusChangeListener(focusListener)
            }
        }
    }
}

package com.example.bilibili.util

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.R as AppCompatR
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.bilibili.R

/**
 * 文字选择：使用 AppCompat 系统自带手柄/光标 Drawable，仅做粉色着色（非自制图标）。
 */
object TextSelectHandleHelper {

    private val applied = mutableSetOf<Int>()

    fun applyPinkHandles(editText: EditText) {
        // 生成身份证
        val token = System.identityHashCode(editText)

        if (!applied.add(token)) return

        // 染色方法
        val apply = Runnable { applyPinkHandlesInternal(editText) }
        if (editText.viewTreeObserver.isAlive) {
            editText.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    apply.run()
                }
            })
        }
        apply.run()
    }

    fun applyPinkHandlesIn(activity: Activity) {
        val root = activity.window?.decorView ?: return
        applyPinkHandlesIn(root)
    }

    /** Dialog / BottomSheet 独立 Window，需在 show 之后对 decorView 调用 */
    fun applyPinkHandlesIn(dialog: Dialog) {
        dialog.window?.decorView?.let { applyPinkHandlesIn(it) }
    }

    fun bindPinkEditTextOnShow(dialog: Dialog) {
        dialog.setOnShowListener {
            applyPinkHandlesIn(it as Dialog)
        }
    }

    fun applyPinkHandlesIn(root: View) {
        root.post {
            forEachEditText(root) { applyPinkHandles(it) }
        }
    }

    /**
     * 遍历所有的输入框
     */
    private fun forEachEditText(view: View, block: (EditText) -> Unit) {
        if (view is EditText) block(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                forEachEditText(view.getChildAt(i), block)
            }
        }
    }

    private fun applyPinkHandlesInternal(editText: EditText) {
        val context = editText.context
        val pink = ContextCompat.getColor(context, R.color.bili_pink)

        // 染粉色光标
        applyPinkCursor(editText, context, pink)
        applyPinkSystemHandles(editText, context, pink)
    }

    /**
     * 应用粉色光标
     */
    private fun applyPinkCursor(editText: EditText, context: android.content.Context, @ColorInt pink: Int) {
        // 使用 mutate 克隆
        val cursor = AppCompatResources.getDrawable(context, R.drawable.cursor_pink_bar)?.mutate() ?: return
        DrawableCompat.setTint(cursor, pink)
        // 不会更改透明颜色
        DrawableCompat.setTintMode(cursor, android.graphics.PorterDuff.Mode.SRC_IN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editText.textCursorDrawable = cursor
        }
    }

    /** AppCompat 内置 text select handle（与系统 Material 一致），仅 tint 为粉色 */
    private fun applyPinkSystemHandles(editText: EditText, context: android.content.Context, @ColorInt pink: Int) {
        val left = tintedAppCompatHandle(context, AppCompatR.drawable.abc_text_select_handle_left_mtrl, pink)
        val right = tintedAppCompatHandle(context, AppCompatR.drawable.abc_text_select_handle_right_mtrl, pink)
        val middle = tintedAppCompatHandle(
            context,
            AppCompatR.drawable.abc_text_select_handle_middle_mtrl,
            pink,
        ) ?: left

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tintIfPresent(editText.textSelectHandleLeft, pink)
            tintIfPresent(editText.textSelectHandleRight, pink)
            tintIfPresent(editText.textSelectHandle, pink)
        }

        val center = middle ?: left
        if (left == null || right == null || center == null) return

        try {
            val editorField = TextView::class.java.getDeclaredField("mEditor").apply { isAccessible = true }
            val editor = editorField.get(editText) ?: return
            val editorClass = editor.javaClass
            bindHandleDrawable(editor, editorClass, "mSelectHandleLeft", left)
            bindHandleDrawable(editor, editorClass, "mSelectHandleRight", right)
            bindHandleDrawable(editor, editorClass, "mSelectHandleCenter", center)
        } catch (_: Exception) {
            // 部分 ROM 字段名不同，主题 colorControlActivated 仍可能生效
        }
    }

    private fun tintedAppCompatHandle(
        context: android.content.Context,
        @DrawableRes resId: Int,
        @ColorInt tint: Int,
    ) = AppCompatResources.getDrawable(context, resId)?.mutate()?.also {
        DrawableCompat.setTint(it, tint)
        DrawableCompat.setTintMode(it, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun tintIfPresent(drawable: android.graphics.drawable.Drawable?, @ColorInt tint: Int) {
        drawable?.mutate()?.let {
            DrawableCompat.setTint(it, tint)
            DrawableCompat.setTintMode(it, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    private fun bindHandleDrawable(
        editor: Any,
        editorClass: Class<*>,
        fieldName: String,
        drawable: android.graphics.drawable.Drawable,
    ) {
        val field = editorClass.getDeclaredField(fieldName).apply { isAccessible = true }
        when (val handle = field.get(editor)) {
            is ImageView -> {
                handle.setImageDrawable(drawable.constantState?.newDrawable()?.mutate() ?: drawable)
                handle.scaleType = ImageView.ScaleType.CENTER
            }
            is android.graphics.drawable.Drawable -> field.set(editor, drawable)
            null -> field.set(editor, drawable)
        }
    }
}

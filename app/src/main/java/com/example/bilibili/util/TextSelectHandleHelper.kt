package com.example.bilibili.util

import android.graphics.drawable.Drawable
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import com.example.bilibili.R

object TextSelectHandleHelper {

    fun applyPinkHandles(editText: EditText) {
        try {
            val editorField = TextViewCompat::class.java.getDeclaredField("mEditor")
            editorField.isAccessible = true
            val editor = editorField.get(editText) ?: return

            val editorClass = editor.javaClass
            val leftField = editorClass.getDeclaredField("mSelectHandleLeft").apply { isAccessible = true }
            val rightField = editorClass.getDeclaredField("mSelectHandleRight").apply { isAccessible = true }
            val centerField = editorClass.getDeclaredField("mSelectHandleCenter").apply { isAccessible = true }

            val context = editText.context
            val pink = context.getColor(R.color.bili_pink)
            leftField.set(editor, loadHandle(context, R.drawable.text_cursor_handle_left, pink))
            rightField.set(editor, loadHandle(context, R.drawable.text_cursor_handle_right, pink))
            centerField.set(editor, loadHandle(context, R.drawable.text_cursor_handle_middle, pink))
        } catch (_: Exception) {
            // 部分机型反射失败时依赖主题中的 drawable
        }
    }

    private fun loadHandle(
        context: android.content.Context,
        resId: Int,
        tint: Int,
    ): Drawable? = AppCompatResources.getDrawable(context, resId)?.mutate()?.also {
        DrawableCompat.setTint(it, tint)
    }
}

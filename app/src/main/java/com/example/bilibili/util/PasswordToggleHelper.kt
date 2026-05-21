package com.example.bilibili.util

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton

object PasswordToggleHelper {

    fun bind(toggle: ImageButton, editText: EditText) {
        var visible = false
        toggle.setImageResource(com.example.bilibili.R.drawable.ic_visibility)
        toggle.setOnClickListener {
            visible = !visible
            editText.transformationMethod = if (visible) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            editText.setSelection(editText.text.length)
            toggle.setImageResource(
                if (visible) com.example.bilibili.R.drawable.ic_visibility_off
                else com.example.bilibili.R.drawable.ic_visibility
            )
        }
    }
}

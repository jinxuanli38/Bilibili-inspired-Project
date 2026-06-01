package com.example.bilibili.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.bilibili.R

object FollowConfirmDialog {

    fun show(
        context: Context,
        nickName: String,
        currentlyFocused: Boolean,
        onConfirm: () -> Unit,
    ) {
        if (currentlyFocused) {
            AlertDialog.Builder(context, R.style.PinkDialogTheme)
                .setTitle("取消关注")
                .setMessage(buildCancelMessage(nickName))
                .setPositiveButton("确定") { _, _ -> onConfirm() }
                .setNegativeButton("取消", null)
                .show()
        } else {
            AlertDialog.Builder(context, R.style.PinkDialogTheme)
                .setTitle("关注提示")
                .setMessage(buildFollowMessage(nickName))
                .setPositiveButton("确定") { _, _ -> onConfirm() }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun buildFollowMessage(nickName: String): String =
        if (nickName.isBlank()) "您确定要关注吗？" else "确定要关注 $nickName 吗？"

    private fun buildCancelMessage(nickName: String): String =
        if (nickName.isBlank()) "您确定要取消关注吗？" else "确定要取消关注 $nickName 吗？"
}

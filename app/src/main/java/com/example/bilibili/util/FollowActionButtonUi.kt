package com.example.bilibili.util

import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.example.bilibili.R
import com.example.bilibili.util.UiUtils.dp

/**
 * 关注按钮样式。
 * @param focusType 0-未关注 1-已互粉 2-已关注（仅我关注对方）
 */
object FollowActionButtonUi {

    private const val COLOR_FOLLOWED_TEXT = "#666666"

    fun bind(textView: TextView, focusType: Int) {
        when (focusType) {
            1 -> applyFollowedCapsule(textView, "已互粉")
            2 -> applyFollowedCapsule(textView, "已关注")
            else -> applyFollowPink(textView)
        }
    }

    fun bindFollowed(textView: TextView, isMutual: Boolean) {
        bind(textView, if (isMutual) 1 else 2)
    }

    /** 我的关注列表：只会是已互粉或已关注 */
    fun bindForFollowingList(textView: TextView, focusType: Int) {
        bind(textView, if (focusType == 1) 1 else 2)
    }

    /** 已互粉 / 已关注：同款浅灰胶囊 + 图标，仅文案不同 */
    private fun applyFollowedCapsule(textView: TextView, label: String) {
        textView.elevation = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.stateListAnimator = null
        }
        textView.text = label
        textView.setBackgroundResource(R.drawable.shape_follow_btn_mutual)
        textView.backgroundTintList = null
        textView.setTextColor(android.graphics.Color.parseColor(COLOR_FOLLOWED_TEXT))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        textView.gravity = Gravity.CENTER

        val isProfileHeader = textView.id == R.id.btn_focus
        val hPad = if (isProfileHeader) 0 else 12.dp
        textView.setPadding(hPad, 0, hPad, 0)
        textView.minHeight = 32.dp
        textView.minWidth = if (isProfileHeader) 0 else 72.dp

        val iconSize = 14.dp
        val icon = ContextCompat.getDrawable(textView.context, R.drawable.ic_follow_status_lines)?.mutate()
        icon?.setTint(android.graphics.Color.parseColor(COLOR_FOLLOWED_TEXT))
        icon?.setBounds(0, 0, iconSize, iconSize)
        textView.compoundDrawablePadding = 6.dp
        textView.setCompoundDrawablesRelative(icon, null, null, null)

        textView.updateLayoutParams<ViewGroup.LayoutParams> {
            width = if (isProfileHeader) {
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            if (this is ConstraintLayout.LayoutParams) {
                horizontalBias = if (isProfileHeader) 0.5f else 1f
            }
        }
    }

    private fun applyFollowPink(textView: TextView) {
        textView.elevation = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.stateListAnimator = null
        }
        textView.text = "关注"
        textView.setBackgroundResource(R.drawable.shape_follow_btn_pink)
        textView.backgroundTintList = null
        textView.setTextColor(android.graphics.Color.parseColor("#FB7299"))
        textView.setCompoundDrawablesRelative(null, null, null, null)
        textView.setPadding(0, 0, 0, 0)
        textView.gravity = Gravity.CENTER

        if (textView.parent is ConstraintLayout && textView.id == R.id.btn_focus) {
            textView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                horizontalBias = 0.5f
            }
        } else {
            textView.updateLayoutParams<ViewGroup.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }
}

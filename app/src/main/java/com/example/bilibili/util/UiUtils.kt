package com.example.bilibili.util

import android.content.res.Resources
import android.util.TypedValue

object UiUtils {
    /**
     * DP 转 PX
     * 使用示例：10.dp
     */
    val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Float 类型的 DP 转 PX
     */
    val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    /**
     * PX 转 DP
     */
    val Int.toDp: Int
        get() = (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    /**
     * SP 转 PX (用于代码设置文字大小)
     */
    val Int.sp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
}
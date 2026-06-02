package com.example.bilibili.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import com.example.bilibili.data.model.ChartPoint
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 轻量折线图：近 7 日数据，支持负值 Y 轴、从左向右绘制动画、滑动查看当日数值气泡。
 */
class SimpleLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FB7299")
        strokeWidth = dp(2f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FB7299")
        style = Paint.Style.FILL
    }
    private val pointStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEEEEE")
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }
    private val zeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDDDDD")
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }
    private val highlightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FB7299")
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = dp(10f)
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = dp(13f)
        textAlign = Paint.Align.CENTER
    }
    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBBBBB")
        textSize = dp(9f)
        textAlign = Paint.Align.RIGHT
    }
    private val bubbleDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#212121")
        textSize = dp(12f)
        typeface = Typeface.DEFAULT_BOLD
    }
    private val bubbleValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = dp(11f)
    }
    private val bubbleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(dp(6f), 0f, dp(2f), Color.parseColor("#33000000"))
    }
    private val bubbleDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FB7299")
        style = Paint.Style.FILL
    }

    private var points: List<ChartPoint> = emptyList()
    private var valueLabel: String = ""
    /** 仅粉丝近 7 日净增可为负；收藏等指标 Y 轴从 0 起 */
    private var allowNegativeAxis = true
    private var selectedIndex: Int? = null
    private var revealProgress = 1f
    private var revealAnimator: ValueAnimator? = null
    private val linePath = Path()
    private val fillPath = Path()
    private val bubbleRect = RectF()
    private val dismissHighlightRunnable = Runnable {
        if (selectedIndex != null) {
            selectedIndex = null
            invalidate()
        }
    }
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var isHorizontalDrag = false

    fun setData(
        data: List<ChartPoint>,
        metricLabel: String = "",
        animate: Boolean = true,
        allowNegativeAxis: Boolean = true,
    ) {
        revealAnimator?.cancel()
        removeCallbacks(dismissHighlightRunnable)
        points = data
        valueLabel = metricLabel
        this.allowNegativeAxis = allowNegativeAxis
        selectedIndex = null
        if (animate && data.isNotEmpty()) {
            revealProgress = 0f
            revealAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = REVEAL_DURATION_MS
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    revealProgress = animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            revealProgress = 1f
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        revealAnimator?.cancel()
        removeCallbacks(dismissHighlightRunnable)
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (points.isEmpty()) return false
        val geometry = computeGeometry() ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                isHorizontalDrag = false
                parent?.requestDisallowInterceptTouchEvent(false)
                updateSelection(event.x, geometry)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(event.x - touchDownX)
                val dy = abs(event.y - touchDownY)
                if (!isHorizontalDrag && (dx > touchSlop || dy > touchSlop)) {
                    isHorizontalDrag = dx > dy
                }
                if (isHorizontalDrag) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    updateSelection(event.x, geometry)
                } else {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHorizontalDrag = false
                parent?.requestDisallowInterceptTouchEvent(false)
                scheduleBubbleDismiss()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateSelection(touchX: Float, geometry: ChartGeometry) {
        val maxIndex = visibleEndIndex(revealProgress)
        val index = indexForTouchX(touchX, geometry).coerceAtMost(maxIndex)
        if (selectedIndex != index) {
            selectedIndex = index
            invalidate()
        }
        scheduleBubbleDismiss()
    }

    private fun scheduleBubbleDismiss() {
        removeCallbacks(dismissHighlightRunnable)
        if (selectedIndex != null) {
            postDelayed(dismissHighlightRunnable, BUBBLE_DISMISS_MS)
        }
    }

    private fun indexForTouchX(touchX: Float, geometry: ChartGeometry): Int {
        if (points.size <= 1) return 0
        val ratio = (touchX - geometry.left) / (geometry.right - geometry.left)
        return (ratio * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
    }

    private fun visibleEndIndex(progress: Float): Int {
        if (points.size <= 1) return 0
        return revealEndFloat(progress).toInt().coerceIn(0, points.lastIndex)
    }

    private fun revealEndFloat(progress: Float): Float {
        if (points.size <= 1) return 0f
        return (points.size - 1) * progress.coerceIn(0f, 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        if (points.isEmpty()) {
            canvas.drawText("暂无图表数据", width / 2f, height / 2f, emptyPaint)
            return
        }

        val geometry = computeGeometry() ?: return

        drawGrid(canvas, geometry)
        drawZeroLine(canvas, geometry)
        drawFill(canvas, geometry)
        drawLine(canvas, geometry)
        drawPoints(canvas, geometry)

        val highlightIndex = selectedIndex
        if (highlightIndex != null && highlightIndex in points.indices &&
            highlightIndex <= visibleEndIndex(revealProgress)
        ) {
            drawHighlight(canvas, geometry, highlightIndex)
        }
    }

    private fun drawGrid(canvas: Canvas, geometry: ChartGeometry) {
        val divisions = geometry.gridDivisions
        for (i in 0..divisions) {
            val y = geometry.top + geometry.chartHeight * i / divisions.toFloat()
            canvas.drawLine(geometry.left, y, geometry.right, y, gridPaint)
            val label = formatYAxisLabel(geometry, i, divisions)
            canvas.drawText(
                label,
                geometry.left - dp(6f),
                y + dp(3f),
                yLabelPaint,
            )
        }
    }

    private fun drawZeroLine(canvas: Canvas, geometry: ChartGeometry) {
        if (geometry.minValue >= 0) return
        val zeroY = valueToY(0, geometry)
        canvas.drawLine(geometry.left, zeroY, geometry.right, zeroY, zeroLinePaint)
    }

    private fun formatYAxisLabel(geometry: ChartGeometry, index: Int, divisions: Int): String {
        val ratio = (divisions - index).toFloat() / divisions.toFloat()
        val value = geometry.minValue + geometry.valueRange * ratio
        return value.roundToInt().toString()
    }

    private fun buildRevealPath(
        geometry: ChartGeometry,
        closeToBottom: Boolean,
        outPath: Path,
    ) {
        outPath.reset()
        if (points.isEmpty()) return

        val endF = revealEndFloat(revealProgress)
        val endI = endF.toInt().coerceIn(0, points.lastIndex)
        val frac = endF - endI

        fun appendPoint(index: Int, x: Float, y: Float) {
            if (index == 0) {
                if (closeToBottom) {
                    outPath.moveTo(x, geometry.bottom)
                    outPath.lineTo(x, y)
                } else {
                    outPath.moveTo(x, y)
                }
            } else {
                outPath.lineTo(x, y)
            }
        }

        for (index in 0..endI) {
            appendPoint(index, geometry.xForIndex(index), valueToY(points[index].value, geometry))
        }

        if (frac > 0f && endI < points.lastIndex) {
            val next = endI + 1
            val x0 = geometry.xForIndex(endI)
            val x1 = geometry.xForIndex(next)
            val y0 = valueToY(points[endI].value, geometry)
            val y1 = valueToY(points[next].value, geometry)
            val x = x0 + (x1 - x0) * frac
            val y = y0 + (y1 - y0) * frac
            appendPoint(next, x, y)
            if (closeToBottom) {
                outPath.lineTo(x, geometry.bottom)
            }
        } else if (closeToBottom && endI >= 0) {
            val x = geometry.xForIndex(endI)
            outPath.lineTo(x, geometry.bottom)
        }

        if (closeToBottom) {
            val startX = geometry.xForIndex(0)
            outPath.lineTo(startX, geometry.bottom)
            outPath.close()
        }
    }

    private fun drawFill(canvas: Canvas, geometry: ChartGeometry) {
        buildRevealPath(geometry, closeToBottom = true, fillPath)
        fillPaint.shader = LinearGradient(
            0f,
            geometry.top,
            0f,
            geometry.bottom,
            Color.parseColor("#33FB7299"),
            Color.parseColor("#00FB7299"),
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(fillPath, fillPaint)
        fillPaint.shader = null
    }

    private fun drawLine(canvas: Canvas, geometry: ChartGeometry) {
        buildRevealPath(geometry, closeToBottom = false, linePath)
        canvas.drawPath(linePath, linePaint)
    }

    private fun drawPoints(canvas: Canvas, geometry: ChartGeometry) {
        val maxIndex = visibleEndIndex(revealProgress)
        val highlightIndex = selectedIndex

        points.forEachIndexed { index, point ->
            if (index > maxIndex) return@forEachIndexed
            if (highlightIndex != null && index == highlightIndex) return@forEachIndexed
            val x = geometry.xForIndex(index)
            val y = valueToY(point.value, geometry)
            canvas.drawCircle(x, y, dp(3f), pointPaint)
        }

        points.forEachIndexed { index, _ ->
            if (index > maxIndex) return@forEachIndexed
            val x = geometry.xForIndex(index)
            val label = points[index].dateLabel
            val textWidth = labelPaint.measureText(label)
            canvas.drawText(
                label,
                x - textWidth / 2f,
                height - paddingBottom - dp(4f),
                labelPaint,
            )
        }
    }

    private fun drawHighlight(canvas: Canvas, geometry: ChartGeometry, index: Int) {
        val point = points[index]
        val x = geometry.xForIndex(index)
        val y = valueToY(point.value, geometry)

        canvas.drawLine(x, geometry.top, x, geometry.bottom, highlightLinePaint)

        canvas.drawCircle(x, y, dp(5f), pointPaint)
        canvas.drawCircle(x, y, dp(5f), pointStrokePaint)

        drawBubble(canvas, x, y, point, geometry)
    }

    private fun drawBubble(
        canvas: Canvas,
        anchorX: Float,
        anchorY: Float,
        point: ChartPoint,
        geometry: ChartGeometry,
    ) {
        val dateText = point.dateLabel
        val metricName = valueLabel.ifEmpty { "数据" }
        val valueText = "$metricName：${point.value}"

        val bubblePaddingH = dp(10f)
        val bubblePaddingV = dp(8f)
        val lineGap = dp(6f)
        val dotRadius = dp(3f)
        val dotGap = dp(4f)

        val dateWidth = bubbleDatePaint.measureText(dateText)
        val valueWidth = dotRadius * 2 + dotGap + bubbleValuePaint.measureText(valueText)
        val bubbleWidth = max(dateWidth, valueWidth) + bubblePaddingH * 2
        val bubbleHeight = bubbleDatePaint.textSize + lineGap + bubbleValuePaint.textSize + bubblePaddingV * 2

        var bubbleLeft = anchorX - bubbleWidth / 2f
        val minLeft = paddingLeft + dp(4f)
        val maxLeft = width - paddingRight - bubbleWidth - dp(4f)
        bubbleLeft = bubbleLeft.coerceIn(minLeft, maxLeft)

        var bubbleBottom = anchorY - dp(12f)
        var bubbleTop = bubbleBottom - bubbleHeight
        val minTop = paddingTop + dp(4f)
        if (bubbleTop < minTop) {
            bubbleTop = minTop
            bubbleBottom = bubbleTop + bubbleHeight
        }

        bubbleRect.set(bubbleLeft, bubbleTop, bubbleLeft + bubbleWidth, bubbleBottom)
        val radius = dp(6f)
        canvas.drawRoundRect(bubbleRect, radius, radius, bubbleBgPaint)

        val contentLeft = bubbleRect.left + bubblePaddingH
        var textY = bubbleRect.top + bubblePaddingV + bubbleDatePaint.textSize
        canvas.drawText(dateText, contentLeft, textY, bubbleDatePaint)

        textY += lineGap + bubbleValuePaint.textSize
        val dotCy = textY - bubbleValuePaint.textSize / 2f
        canvas.drawCircle(contentLeft + dotRadius, dotCy, dotRadius, bubbleDotPaint)
        canvas.drawText(valueText, contentLeft + dotRadius * 2 + dotGap, textY, bubbleValuePaint)
    }

    private fun computeGeometry(): ChartGeometry? {
        val left = paddingLeft + dp(28f)
        val right = width - paddingRight - dp(20f)
        val top = paddingTop + dp(52f)
        val bottom = height - paddingBottom - dp(22f)
        val chartHeight = bottom - top
        if (chartHeight <= 0f || right <= left) return null

        val dataMin = points.minOf { it.value }
        val dataMax = points.maxOf { it.value }
        var minValue: Int
        var maxValue: Int
        if (allowNegativeAxis) {
            minValue = min(dataMin, 0)
            maxValue = max(dataMax, 0)
            if (dataMin == 0 && dataMax == 0) {
                minValue = -1
                maxValue = 1
            } else if (minValue == maxValue) {
                if (maxValue >= 0) minValue -= 1 else maxValue += 1
            }
        } else {
            minValue = max(0, dataMin)
            maxValue = max(0, dataMax)
            if (minValue == 0 && maxValue == 0) {
                maxValue = 1
            } else if (minValue == maxValue) {
                maxValue += 1
            }
        }
        val valueRange = (maxValue - minValue).toFloat().coerceAtLeast(1f)

        val stepX = if (points.size <= 1) 0f else (right - left) / (points.size - 1)
        return ChartGeometry(
            left = left,
            right = right,
            top = top,
            bottom = bottom,
            chartHeight = chartHeight,
            stepX = stepX,
            minValue = minValue,
            maxValue = maxValue,
            valueRange = valueRange,
            gridDivisions = 4,
        )
    }

    private data class ChartGeometry(
        val left: Float,
        val right: Float,
        val top: Float,
        val bottom: Float,
        val chartHeight: Float,
        val stepX: Float,
        val minValue: Int,
        val maxValue: Int,
        val valueRange: Float,
        val gridDivisions: Int,
    ) {
        fun xForIndex(index: Int): Float = left + stepX * index
    }

    private fun valueToY(value: Int, geometry: ChartGeometry): Float {
        val ratio = (value - geometry.minValue) / geometry.valueRange
        return geometry.bottom - ratio * geometry.chartHeight
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    companion object {
        private const val BUBBLE_DISMISS_MS = 3_000L
        private const val REVEAL_DURATION_MS = 650L
    }
}

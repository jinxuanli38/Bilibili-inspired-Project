package com.example.bilibili.ui.message

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object MessageTimeFormatter {

    private val inputPatterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )

    fun format(createTimeRaw: String): String {
        if (createTimeRaw.isBlank()) return ""
        val date = parseDate(createTimeRaw) ?: return createTimeRaw
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        val dayDiff = dayDiff(now, target)
        return when {
            dayDiff == 0 -> "今天"
            dayDiff == 1 -> "昨天"
            dayDiff in 2..6 -> "${dayDiff}天前"
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) ->
                SimpleDateFormat("M月d日", Locale.CHINA).format(date)
            else -> SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(date)
        }
    }

    private fun parseDate(raw: String): java.util.Date? {
        for (pattern in inputPatterns) {
            try {
                return SimpleDateFormat(pattern, Locale.CHINA).parse(raw.trim())
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun dayDiff(a: Calendar, b: Calendar): Int {
        val startA = a.clone() as Calendar
        val startB = b.clone() as Calendar
        startA.set(Calendar.HOUR_OF_DAY, 0)
        startA.set(Calendar.MINUTE, 0)
        startA.set(Calendar.SECOND, 0)
        startA.set(Calendar.MILLISECOND, 0)
        startB.set(Calendar.HOUR_OF_DAY, 0)
        startB.set(Calendar.MINUTE, 0)
        startB.set(Calendar.SECOND, 0)
        startB.set(Calendar.MILLISECOND, 0)
        val diffMs = startA.timeInMillis - startB.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    }
}

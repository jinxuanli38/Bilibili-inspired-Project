package com.example.bilibili.data.model

import java.util.UUID

data class ReleaseVideoPart(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val fileName: String,
    val duration: Long,
    var displayTitle: String,
    var uploadId: String = "",
    var uploadProgress: Int = 0,
    var uploadStatus: String = "",
) {
    val partLabel: String
        get() = "P$displayIndex"

    var displayIndex: Int = 1

    fun shortFileName(maxLen: Int = 14): String {
        val name = displayTitle.ifBlank { fileName }
        return if (name.length <= maxLen) name else name.take(maxLen) + "..."
    }
}

package com.example.bilibili.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream

object VideoThumbnailUtil {

    /**
     * 获取视频第一帧并保存为图片文件
     * @param context 上下文
     * @param videoPath 视频文件路径
     * @return 保存的图片文件路径
     */
    fun getFirstFrame(context: Context, videoPath: String): String? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)

            // 获取第一帧（时间设为0）
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            if (bitmap != null) {
                // 保存第一帧为临时图片文件
                val outputFile = File(context.cacheDir, "video_cover_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(outputFile)

                // 压缩保存为JPEG格式
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.close()

                return outputFile.absolutePath
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever?.release()
        }

        return null
    }

    /**
     * 获取视频第一帧Bitmap
     * @param videoPath 视频文件路径
     * @return 第一帧Bitmap
     */
    fun getFirstFrameBitmap(videoPath: String): Bitmap? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)

            // 获取第一帧（时间设为0）
            return retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever?.release()
        }

        return null
    }
}
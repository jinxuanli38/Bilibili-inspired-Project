package com.example.bilibili.util

import org.apache.commons.codec.digest.DigestUtils

/**
 * MD5 加密工具类
 */
object MD5Utils {

    /**
     * 基础 MD5 加密 (32位小写)
     * @param text 需要加密的明文
     * @return 32位十六进制字符串
     */
    fun encrypt(text: String): String {
        if (text.isEmpty()) return ""
        return DigestUtils.md5Hex(text)
    }
}
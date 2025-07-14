package com.tisan.share.datdabean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EncryptedFileItem(
    val fileName: String,
    val originalName: String,
    val mimeType: String,
    val timestamp: Long,
    val filePath: String, // 加密文件的完整路径
    val thumbPath: String? = null // ✅ 新增字段
): Parcelable {
    fun getCategory(): String = when {
        mimeType.startsWith("image/") -> "图片"
        mimeType.startsWith("audio/") -> "音频"
        mimeType.startsWith("video/") -> "视频"
        else -> "其他文件"
    }
}

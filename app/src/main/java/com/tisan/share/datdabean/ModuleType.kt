package com.tisan.share.datdabean

enum class ModuleType {
    IMAGE,     // 图片类
    VIDEO,     // 视频类
    AUDIO,     // 音频类
    DOCUMENT;   // 文档类（包含压缩包、PDF等）

    companion object {
        fun fromString(typeStr: String): ModuleType? = when(typeStr.uppercase()) {
            "IMAGE" -> IMAGE
            "VIDEO" -> VIDEO
            "AUDIO" -> AUDIO
            "DOCUMENT" -> DOCUMENT
            else -> null
        }
    }
}

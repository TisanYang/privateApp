package com.tisan.share.datdabean

data class FileModuleItem(
    val type: ModuleType,                      // 模块类型（IMAGE, VIDEO, DOCUMENT...）
    val title: String,                         // 模块标题（展示用）
    val files: List<EncryptedFileItem>         // 模块内的加密文件列表（内容项）
)

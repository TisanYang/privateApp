package com.tisan.share.data

import android.content.Context
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.io.File

object FileRepository {

    // 缓存所有模块
    private val moduleCache = mutableListOf<FileModuleItem>()
    private val fullModuleCache = mutableListOf<FileModuleItem>()

    // 是否初始化
    fun isInitialized() = moduleCache.isNotEmpty()

    private val _refreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvent = _refreshEvent.asSharedFlow()


    fun updateCache(preview: List<FileModuleItem>, full: List<FileModuleItem>) {
        moduleCache.clear()
        moduleCache.addAll(preview)

        fullModuleCache.clear()
        fullModuleCache.addAll(full)
    }

    fun getPreviewModules(): List<FileModuleItem> = moduleCache.toList()
    fun getFullModules(): List<FileModuleItem> = fullModuleCache.toList()

    suspend fun notifyDataChanged() {
        _refreshEvent.emit(Unit)
    }

    // 加载所有模块（假设你有文件扫描逻辑）
//    fun loadAllModules(context: Context): List<FileModuleItem> {
//        if (moduleCache.isNotEmpty()) return moduleCache
//
//        val dir = context.filesDir
//        val encFiles = dir.listFiles { file -> file.name.endsWith(".enc") } ?: return emptyList()
//
//        val fileList = encFiles.mapNotNull { encFile ->
//            val metaFile = File(encFile.absolutePath + ".meta")
//            if (!metaFile.exists()) return@mapNotNull null
//
//            val meta = JSONObject(metaFile.readText())
//            val thumbFile = File(encFile.parentFile, encFile.name + ".thumb")
//            val hasThumb = thumbFile.exists()
//
//            EncryptedFileItem(
//                fileName = encFile.name,
//                originalName = meta.optString("originalName", encFile.name),
//                mimeType = meta.optString("mimeType", "application/octet-stream"),
//                timestamp = meta.optLong("timestamp", encFile.lastModified()),
//                filePath = encFile.absolutePath,
//                thumbPath = if (hasThumb) thumbFile.absolutePath else null
//            )
//        }
//
//        val groupedMap = fileList.groupBy { it.getCategory() }
//
//        val orderedCategories = listOf("图片", "视频", "音频", "其他文件")
//
//        val modules = orderedCategories.mapNotNull { category ->
//            val type = when (category) {
//                "图片" -> ModuleType.IMAGE
//                "视频" -> ModuleType.VIDEO
//                "音频" -> ModuleType.AUDIO
//                "其他文件" -> ModuleType.DOCUMENT
//                else -> null
//            }
//            groupedMap[category]?.takeIf { it.isNotEmpty() }?.let { filesInCategory ->
//                val sortedFiles = filesInCategory.sortedByDescending { it.timestamp }
//
//                val limitedFiles = if (sortedFiles.size > 6) {
//                    val previewFiles = sortedFiles.take(5).toMutableList()
//                    previewFiles.add(
//                        EncryptedFileItem(
//                            fileName = "...",
//                            originalName = "...",
//                            mimeType = "more/placeholder",
//                            timestamp = 0L,
//                            filePath = ""
//                        )
//                    )
//                    previewFiles
//                } else {
//                    sortedFiles
//                }
//
//                FileModuleItem(
//                    type = type!!,
//                    title = category,
//                    files = limitedFiles
//                )
//            }
//        }
//
//        moduleCache.addAll(modules)
//        return moduleCache
//    }


    // 通过模块类型筛选模块
    fun getModulesByType(type: ModuleType?): List<FileModuleItem> {
        return if (type == null) {
            fullModuleCache.toList()
        } else {
            fullModuleCache.filter { it.type == type }
        }
    }

    // 新增模块（一般用得少，或者只在初始化时）
    fun addModule(module: FileModuleItem) {
        fullModuleCache.add(0, module)
    }

    // 删除模块
    fun removeModuleByType(type: ModuleType) {
        fullModuleCache.removeAll { it.type == type }
    }

    // 更新模块，比如更新文件列表
    fun updateModule(updatedModule: FileModuleItem) {
        val idx = fullModuleCache.indexOfFirst { it.type == updatedModule.type }
        if (idx != -1) {
            fullModuleCache[idx] = updatedModule
        }
    }

    // 增删改模块内文件示例（更新模块文件列表）
    fun addFileToModule(type: ModuleType, file: EncryptedFileItem) {
        val idx = fullModuleCache.indexOfFirst { it.type == type }
        if (idx != -1) {
            val oldModule = fullModuleCache[idx]
            val newFiles = listOf(file) + oldModule.files
            fullModuleCache[idx] = oldModule.copy(files = newFiles)
        }
    }

    fun removeFileFromModule(type: ModuleType, filePath: String) {
        val idx = fullModuleCache.indexOfFirst { it.type == type }
        if (idx != -1) {
            val oldModule = fullModuleCache[idx]
            val newFiles = oldModule.files.filterNot { it.filePath == filePath }
            fullModuleCache[idx] = oldModule.copy(files = newFiles)
        }
    }

    fun clearCache() {
        fullModuleCache.clear()
    }
}

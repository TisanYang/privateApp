package com.tisan.share.vm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.data.FileRepository
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.utils.CryptoUtil
import com.tisan.share.utils.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

object VaultViewModel : BaseViewModel() {

    private val _modules = MutableLiveData<List<FileModuleItem>>()
    val modules: LiveData<List<FileModuleItem>> = _modules

    private val _fullData = MutableLiveData<List<FileModuleItem>>()
    val fullData: LiveData<List<FileModuleItem>> = _fullData


    // 使用 StateFlow 保持最新状态
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()

    private val _importResult = MutableSharedFlow<String>()
    val importResult: SharedFlow<String> = _importResult.asSharedFlow()

    // 获取原始文件名
    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val name = nameIndex?.let { cursor.getString(it) }
        cursor?.close()
        return name
    }

    fun loadAllEncryptedFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = context.filesDir
            val encFiles = dir.listFiles { file -> file.name.endsWith(".enc") } ?: return@launch

            val fileList = encFiles.mapNotNull { encFile ->
                val metaFile = File(encFile.absolutePath + ".meta")
                if (!metaFile.exists()) return@mapNotNull null

                val meta = JSONObject(metaFile.readText())

                // 👇 构造 thumb 路径
                val thumbFile = File(encFile.parentFile, encFile.name + ".thumb")
                val hasThumb = thumbFile.exists()

                //CryptoUtil.recompressThumbFile(thumbFile)

                EncryptedFileItem(
                    fileName = encFile.name,
                    originalName = meta.optString("originalName", encFile.name),
                    mimeType = meta.optString("mimeType", "application/octet-stream"),
                    timestamp = meta.optLong("timestamp", encFile.lastModified()),
                    filePath = encFile.absolutePath, // 👈 加上这个
                    thumbPath = if (hasThumb) thumbFile.absolutePath else null
                )
            }

            // 分组分类
            val groupedMap = fileList.groupBy { it.getCategory() }

            // 排序并封装为模块列表（List<FileModuleItem>）
            val orderedCategories = listOf("图片", "视频", "音频", "其他文件")

            //把上面的list进行遍历
            val modules = orderedCategories.mapNotNull { category ->
                val type = when (category) {
                    "图片" -> ModuleType.IMAGE
                    "视频" -> ModuleType.VIDEO
                    "音频" -> ModuleType.AUDIO
                    "其他文件" -> ModuleType.DOCUMENT
                    else -> null
                }
                groupedMap[category]?.takeIf { it.isNotEmpty() }?.let { filesInCategory ->
                    val sortedFiles = filesInCategory.sortedByDescending { it.timestamp }

                    val limitedFiles = if (sortedFiles.size > 6) {
                        val previewFiles = sortedFiles.take(5).toMutableList()
                        previewFiles.add(
                            EncryptedFileItem(
                                fileName = "...",
                                originalName = "...",
                                mimeType = "more/placeholder",
                                timestamp = 0L,
                                filePath = "" // 可以用空路径或特殊标志识别
                            )
                        )
                        previewFiles
                    } else {
                        sortedFiles
                    }

                    FileModuleItem(
                        type = type!!,
                        title = category,
                        files = limitedFiles
                    )
                }
            }

            //把上面的list进行遍历
            val modulesAll = orderedCategories.mapNotNull { category ->
                val type = when (category) {
                    "图片" -> ModuleType.IMAGE
                    "视频" -> ModuleType.VIDEO
                    "音频" -> ModuleType.AUDIO
                    "其他文件" -> ModuleType.DOCUMENT
                    else -> null
                }
                groupedMap[category]?.takeIf { it.isNotEmpty() }?.let { filesInCategory ->
                    val sortedFiles = filesInCategory.sortedByDescending { it.timestamp }

                    FileModuleItem(
                        type = type!!,
                        title = category,
                        files = sortedFiles
                    )
                }
            }

            // 发布到 UI
            withContext(Dispatchers.Main) {
                _modules.value = modules
                _fullData.value = modulesAll

                // ✅ 更新 FileRepository 缓存
                FileRepository.updateCache(modules, modulesAll)
                EventBus.updateCacheData.emit(Unit)
            }
        }
    }

    fun importEncryptedFiles(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            var success = 0
            var duplicate = 0
            var failed = 0
            val total = uris.size

            uris.forEachIndexed { index, uri ->
                when (importSingleFile(context, uri)) {
                    ImportResult.SUCCESS -> success++
                    ImportResult.DUPLICATE -> duplicate++
                    ImportResult.FAIL -> failed++
                }
                _importProgress.emit(ImportProgress(index + 1, total))
            }

            val result = buildString {
                if (success > 0) append("成功导入 $success 个文件\n")
                if (duplicate > 0) append("$duplicate 个文件已存在\n")
                if (failed > 0) append("导入失败 $failed 个文件")
            }.trim()

            _importResult.emit(result)

            if (success > 0) {
                withContext(Dispatchers.Main) {
                    loadAllEncryptedFiles(context)
                    FileRepository.notifyDataChanged()
                }
            }

            // 导入结束后重置进度为 null（可选）
            _importProgress.emit(null)
        }
    }


    private suspend fun importSingleFile(context: Context, uri: Uri): ImportResult {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return ImportResult.FAIL

            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
            inputStream.close()
            val fileHash = digest.digest().joinToString("") { "%02x".format(it) }

            val metaFiles =
                context.filesDir.listFiles { it -> it.name.endsWith(".meta") } ?: emptyArray()
            val duplicate = metaFiles.any {
                try {
                    val metaJson = JSONObject(it.readText())
                    metaJson.optString("hash") == fileHash
                } catch (_: Exception) {
                    false
                }
            }
            if (duplicate) return ImportResult.DUPLICATE

            val timestamp = System.currentTimeMillis()
            val encFileName = "vault_$timestamp.enc"
            val encFile = File(context.filesDir, encFileName)

            val encryptStream = contentResolver.openInputStream(uri) ?: return ImportResult.FAIL
            CryptoUtil.encryptFile(encryptStream, encFile)
            encryptStream.close()

            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val originalName = getFileName(context, uri) ?: "unknown"

            val meta = JSONObject().apply {
                put("originalName", originalName)
                put("mimeType", mimeType)
                put("timestamp", timestamp)
                put("hash", fileHash)
            }
            val metaFile = File(context.filesDir, "$encFileName.meta")
            metaFile.writeText(meta.toString())

            // ✅ 生成并加密缩略图（新增！）
            if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                val thumbBitmap = if (mimeType.startsWith("image/")) {
                    BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                } else {
                    generateVideoThumbnail(context, uri)
                }

                thumbBitmap?.let {
                    val thumbFile = File(context.filesDir, "$encFileName.thumb")
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val encryptedThumb = CryptoUtil.encrypt(baos.toByteArray())
                    thumbFile.writeBytes(encryptedThumb)
                }
            }

            ImportResult.SUCCESS
        } catch (e: Exception) {
            ImportResult.FAIL
        }
    }

    enum class ImportResult { SUCCESS, DUPLICATE, FAIL }

    fun generateVideoThumbnail(context: Context, uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

}

// 进度数据类
data class ImportProgress(val current: Int, val total: Int)


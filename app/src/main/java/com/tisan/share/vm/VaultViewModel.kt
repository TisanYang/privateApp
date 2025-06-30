package com.tisan.share.vm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.utils.CryptoUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

object VaultViewModel : BaseViewModel() {

    private val _modules = MutableLiveData<List<FileModuleItem>>()
    val modules: LiveData<List<FileModuleItem>> = _modules

    /*fun importEncryptedFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val fileBytes = inputStream.readBytes()
                inputStream.close()

                // 原始文件信息
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val originalName = getFileName(context, uri) ?: "unknown"

                // ✅ Step 1: 计算文件哈希（用于查重）
                val fileHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(fileBytes)

                // ✅ Step 2: 查重（遍历已有 .meta 文件）
                val metaFiles = context.filesDir.listFiles{ it -> it.name.endsWith(".meta") } ?: emptyArray()
                val duplicate = metaFiles.any { metaFile ->
                    try {
                        val metaJson = JSONObject(metaFile.readText())
                        metaJson.optString("hash") == fileHash
                    } catch (e: Exception) {
                        false
                    }
                }

                if (duplicate) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "该文件已导入，无需重复添加", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 加密
                val encryptedBytes = CryptoUtil.encrypt(fileBytes)

                // 加密文件保存
                val timestamp = System.currentTimeMillis()
                val encFileName = "vault_$timestamp.enc"
                val encFile = File(context.filesDir, encFileName)
                encFile.writeBytes(encryptedBytes)

                // 保存元数据
                val meta = JSONObject().apply {
                    put("originalName", originalName)
                    put("mimeType", mimeType)
                    put("timestamp", timestamp)
                    put("hash", fileHash) // 🔥 关键
                }
                val metaFile = File(context.filesDir, "$encFileName.meta")
                metaFile.writeText(meta.toString())

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                    loadAllEncryptedFiles(context)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }*/

    fun importEncryptedFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch

                // ✅ Step 1: 计算文件哈希（流式读入并哈希）
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    digest.update(buffer, 0, read)
                }
                inputStream.close()
                val fileHash = digest.digest().joinToString("") { "%02x".format(it) }

                // ✅ Step 2: 查重
                val metaFiles = context.filesDir.listFiles {it -> it.name.endsWith(".meta") } ?: emptyArray()
                val duplicate = metaFiles.any { metaFile ->
                    try {
                        val metaJson = JSONObject(metaFile.readText())
                        metaJson.optString("hash") == fileHash
                    } catch (e: Exception) {
                        false
                    }
                }
                if (duplicate) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "该文件已导入，无需重复添加", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // ✅ Step 3: 加密文件保存
                val timestamp = System.currentTimeMillis()
                val encFileName = "vault_$timestamp.enc"
                val encFile = File(context.filesDir, encFileName)

                // 用流式加密保存到目标文件
                val inputStreamForEncrypt = contentResolver.openInputStream(uri) ?: return@launch
                CryptoUtil.encryptFile(inputStreamForEncrypt, encFile)
                inputStreamForEncrypt.close()

                // ✅ Step 4: 保存元数据
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

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                    loadAllEncryptedFiles(context)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    // 获取原始文件名
    private fun getFileName(context: Context, uri: Uri): String? {
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

                EncryptedFileItem(
                    fileName = encFile.name,
                    originalName = meta.optString("originalName", encFile.name),
                    mimeType = meta.optString("mimeType", "application/octet-stream"),
                    timestamp = meta.optLong("timestamp", encFile.lastModified()),
                    filePath = encFile.absolutePath // 👈 加上这个
                )
            }

            // 分组分类
            val groupedMap = fileList.groupBy { it.getCategory() }

            // 排序并封装为模块列表（List<FileModuleItem>）
            val orderedCategories = listOf("图片", "视频", "音频", "其他文件")

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
                        title = "隐藏$category",
                        files = limitedFiles
                    )
                }
            }

            // 发布到 UI
            withContext(Dispatchers.Main) {
                _modules.value = modules
            }
        }
    }
}

package com.tisan.share.vm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.tisan.share.utils.CryptoUtil
import com.tisan.share.utils.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

//object FunctionViewModel : ViewModel() {
//}
@HiltViewModel
class FunctionViewModel @Inject constructor() : ViewModel() {

    val importResult = MutableStateFlow("")
    val importProgress = MutableStateFlow<ImportProgress?>(null)

    data class ImportProgress(val current: Int, val total: Int)

    enum class ImportResult { SUCCESS, DUPLICATE, FAIL }

    fun importEncryptedFile(context: Context, uri: Uri) {
        importEncryptedFiles(context, listOf(uri))
    }

    private fun importEncryptedFiles(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            var success = 0
            var duplicate = 0
            var fail = 0
            val total = uris.size

            uris.forEachIndexed { index, uri ->
                when (importSingleFile(context, uri)) {
                    ImportResult.SUCCESS -> success++
                    ImportResult.DUPLICATE -> duplicate++
                    ImportResult.FAIL -> fail++
                }
                importProgress.emit(ImportProgress(index + 1, total))
            }

            importResult.emit(buildString {
                if (success > 0) append("成功导入 $success 个\n")
                if (duplicate > 0) append("$duplicate 个已存在\n")
                if (fail > 0) append("失败 $fail 个")
            })

            importProgress.emit(null)
            EventBus.fileListUpdated.emit(Unit)
        }
    }

    private fun importSingleFile(context: Context, uri: Uri): ImportResult {
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

            // 查重
            val metaFiles =
                context.filesDir.listFiles { f -> f.name.endsWith(".meta") } ?: emptyArray()
            if (metaFiles.any {
                    runCatching {
                        JSONObject(it.readText()).optString("hash") == fileHash
                    }.getOrDefault(false)
                }) return ImportResult.DUPLICATE

            // 保存加密文件
            val timestamp = System.currentTimeMillis()
            val encFileName = "vault_$timestamp.enc"
            val encFile = File(context.filesDir, encFileName)

            val encryptStream = contentResolver.openInputStream(uri) ?: return ImportResult.FAIL
            CryptoUtil.encryptFile(encryptStream, encFile)
            encryptStream.close()

            //val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val mimeType = guessMimeType(context, uri)
            val originalName = getFileName(context, uri) ?: "unknown"

            // 缩略图
            if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                val thumbnail = if (mimeType.startsWith("image/")) {
                    BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                } else {
                    generateVideoThumbnail(context, uri)
                }
                thumbnail?.let {
                    val thumbStream = File(context.filesDir, "$encFileName.thumb")
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    thumbStream.writeBytes(CryptoUtil.encrypt(baos.toByteArray()))
                }
            }

            // 元信息
            val meta = JSONObject().apply {
                put("originalName", originalName)
                put("mimeType", mimeType)
                put("timestamp", timestamp)
                put("hash", fileHash)
            }
            File(context.filesDir, "$encFileName.meta").writeText(meta.toString())

            ImportResult.SUCCESS
        } catch (e: Exception) {
            ImportResult.FAIL
        }
    }

    private fun generateVideoThumbnail(context: Context, uri: Uri): Bitmap? {
        return runCatching {
            MediaMetadataRetriever().apply {
                setDataSource(context, uri)
            }.frameAtTime
        }.getOrNull()
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return name ?: uri.path?.substringAfterLast('/')
    }

    private fun guessMimeType(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)

        if (mimeType != null && mimeType != "application/octet-stream") {
            return mimeType
        }

        // 如果路径中带 "photo_" 且后缀是 jpg，则强制返回 image/jpeg
        val path = uri.path ?: return "application/octet-stream"
        if (path.contains("photo_") && path.endsWith(".jpg")) {
            return "image/jpeg"
        }

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(path)
        ) ?: "application/octet-stream"
    }

}

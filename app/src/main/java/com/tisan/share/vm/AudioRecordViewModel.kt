package com.tisan.share.vm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.utils.CryptoUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class AudioRecordViewModel(
    private val context: Context
) : BaseViewModel() {

    suspend fun importSingleFile(context: Context, uri: Uri): VaultViewModel.ImportResult {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return VaultViewModel.ImportResult.FAIL

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
            if (duplicate) return VaultViewModel.ImportResult.DUPLICATE

            val timestamp = System.currentTimeMillis()
            val encFileName = "vault_$timestamp.enc"
            val encFile = File(context.filesDir, encFileName)

            val encryptStream = contentResolver.openInputStream(uri) ?: return VaultViewModel.ImportResult.FAIL
            CryptoUtil.encryptFile(encryptStream, encFile)
            encryptStream.close()

            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val originalName = VaultViewModel.getFileName(context, uri) ?: "unknown"

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
                    VaultViewModel.generateVideoThumbnail(context, uri)
                }

                thumbBitmap?.let {
                    val thumbFile = File(context.filesDir, "$encFileName.thumb")
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val encryptedThumb = CryptoUtil.encrypt(baos.toByteArray())
                    thumbFile.writeBytes(encryptedThumb)
                }
            }

            VaultViewModel.ImportResult.SUCCESS
        } catch (e: Exception) {
            VaultViewModel.ImportResult.FAIL
        }
    }


    private fun getAudioDuration(file: File): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toIntOrNull()?.div(1000) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun notifyFileListUpdated() {
        // 举例，假设BaseViewModel里有一个单向事件LiveData叫 eventBus
        // eventBus.postValue(Event.FileListUpdated)
        // 或者你自己定义一个 LiveData 通知页面刷新
    }
}

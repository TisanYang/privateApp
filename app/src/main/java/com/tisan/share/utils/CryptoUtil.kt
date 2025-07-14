package com.tisan.share.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"
    private const val SECRET_KEY = "1234567890123456" // 16位密钥
    private const val FILE_SIZE_THRESHOLD = 1 * 1024 * 1024 // 1MB：大于就用流式处理

    private fun getSecretKeySpec(): SecretKeySpec {
        return SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
    }

    // 保持原有接口，用于加密小文件（byteArray）
    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec())
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec())
        return cipher.doFinal(data)
    }

    // 新增：流式加密（用于大文件或主动调用）
    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec())

        CipherOutputStream(FileOutputStream(outputFile), cipher).use { cipherOut ->
            FileInputStream(inputFile).use { input ->
                input.copyTo(cipherOut)
            }
        }
    }

    // 新增：流式解密
    fun decryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec())

        CipherInputStream(FileInputStream(inputFile), cipher).use { cipherIn ->
            FileOutputStream(outputFile).use { output ->
                cipherIn.copyTo(output)
            }
        }
    }

    // 新增：自动判断文件大小，加密为 ByteArray
    fun encryptFileToByteArray(file: File): ByteArray {
        return if (file.length() <= FILE_SIZE_THRESHOLD) {
            encrypt(file.readBytes())
        } else {
            val temp = File.createTempFile("enc_", null)
            encryptFile(file, temp)
            val result = temp.readBytes()
            temp.delete()
            result
        }
    }

    fun decryptFileToByteArray(file: File): ByteArray {
        return if (file.length() <= FILE_SIZE_THRESHOLD) {
            decrypt(file.readBytes())
        } else {
            val temp = File.createTempFile("dec_", null)
            decryptFile(file, temp)
            val result = temp.readBytes()
            temp.delete()
            result
        }
    }

    // 添加到 CryptoUtil 中（已提供大文件方案中的版本）
    fun encryptFile(inputStream: InputStream, outputFile: File) {
        val cipher = Cipher.getInstance("AES")
        val keySpec = SecretKeySpec("1234567890123456".toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        CipherOutputStream(FileOutputStream(outputFile), cipher).use { cipherOut ->
            inputStream.copyTo(cipherOut)
        }
    }

    fun getCipher(mode: Int): Cipher {
        val key = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(mode, key)
        return cipher
    }

    fun encrypt(input: InputStream, output: OutputStream) {
        val cipher = getCipher(Cipher.ENCRYPT_MODE)
        CipherOutputStream(output, cipher).use { cipherOut ->
            input.copyTo(cipherOut)
        }
    }

    fun decrypt(input: InputStream, output: OutputStream) {
        val cipher = getCipher(Cipher.DECRYPT_MODE)
        CipherInputStream(input, cipher).use { cipherIn ->
            cipherIn.copyTo(output)
        }
    }

    fun generateThumbFileOptimized(encryptedFile: File, thumbFile: File) {
        // 解密原始字节数据
        val decryptedBytes = CryptoUtil.decrypt(encryptedFile.readBytes())

        // Step 1: 先读取宽高信息，不解码 Bitmap（避免占内存）
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size, boundsOptions)

        val maxDim = 300 // 设置你期望的缩略图尺寸
        val (originalW, originalH) = boundsOptions.outWidth to boundsOptions.outHeight

        // Step 2: 计算采样率
        var inSampleSize = 1
        if (originalH > maxDim || originalW > maxDim) {
            val halfH = originalH / 2
            val halfW = originalW / 2
            while ((halfH / inSampleSize) > maxDim && (halfW / inSampleSize) > maxDim) {
                inSampleSize *= 2
            }
        }

        // Step 3: 重新解码为小图（低内存）
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            this.inPreferredConfig = Bitmap.Config.RGB_565 // 更省内存（16位色）
        }

        val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size, decodeOptions)
            ?: throw IllegalStateException("Failed to decode bitmap")

        // Step 4: 写入 thumb 文件
        thumbFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }

        // Step 5: 回收 bitmap，防止内存泄漏
        bitmap.recycle()
    }



}


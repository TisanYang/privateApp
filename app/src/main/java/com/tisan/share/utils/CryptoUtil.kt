package com.tisan.share.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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

}


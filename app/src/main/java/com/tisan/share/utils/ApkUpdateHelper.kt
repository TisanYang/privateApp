package com.tisan.share.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ApkUpdateHelper {

    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    private const val REQUEST_CODE_INSTALL = 1001

    fun downloadAndInstallApk(context: Context, apkUrl: String, apkFileName: String = "update.apk") {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_INSTALL
            )
            return
        }

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkFileName)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(apkUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val contentLength = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(file)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    // Progress log
                    val progress = (total * 100 / contentLength).toInt()
                    Log.d("ApkUpdate", "Download progress: $progress%")
                }

                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    installApk(context, FileProvider.getUriForFile(context, context.packageName + ".provider", file))
                }
            } catch (e: Exception) {
                Log.e("ApkUpdate", "Download failed: ${e.message}")
            }
        }
    }

    private fun installApk(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray, onGranted: () -> Unit) {
        if (requestCode == REQUEST_CODE_INSTALL &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        }
    }
}

package com.tisan.share.dapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kj.infinite.R
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.utils.CryptoUtil
import java.io.File

class EncryptedFileAdapter(
    private val items: List<EncryptedFileItem>
) : RecyclerView.Adapter<EncryptedFileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_encrypted_file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(items[position])
        }
    }

    var onItemClick: ((EncryptedFileItem) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val thumb = view.findViewById<ImageView>(R.id.fileThumb)
        private val name = view.findViewById<TextView>(R.id.fileName)

        fun bind(item: EncryptedFileItem) {

            if (item.mimeType == "special/more") {
                // 显示为“...”
                name.text = "更多..."
                thumb.setImageResource(R.drawable.ic_launcher_background) // 一张“...”的图片
            } else {
                name.text = item.originalName.truncate(10)


                if (!item.thumbPath.isNullOrEmpty()) {
                    // ✅ 有缩略图，解密后显示
                    val encryptedThumbBytes = File(item.thumbPath).readBytes()
                    val decryptedThumb = CryptoUtil.decrypt(encryptedThumbBytes)
                    val bitmap =
                        BitmapFactory.decodeByteArray(decryptedThumb, 0, decryptedThumb.size)
                    thumb.setImageBitmap(bitmap)
                } else {
                    // 解密临时文件路径
                    val tempFile = File.createTempFile("dec_", ".jpg", itemView.context.cacheDir)
                    val encryptedFile = File(item.filePath)

                    try {
//                    val encryptedBytes = encryptedFile.readBytes()
//                    val decryptedBytes = CryptoUtil.decrypt(encryptedBytes)
//                    tempFile.writeBytes(decryptedBytes)
                        CryptoUtil.decryptFile(encryptedFile, tempFile)

                        // 用 Glide 加载解密后的图像
                        Glide.with(itemView.context).load(tempFile)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background).into(thumb)

                    } catch (e: Exception) {
                        // 加载失败时可以显示默认图
                        thumb.setImageResource(R.drawable.ic_launcher_background)
                    }
                }
            }
        }
    }
}

fun String.truncate(maxLength: Int): CharSequence {
    return if (this.length <= maxLength) this else this.substring(0, maxLength) + "..."
}

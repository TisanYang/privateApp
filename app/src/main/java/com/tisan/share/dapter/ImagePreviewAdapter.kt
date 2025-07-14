package com.tisan.share.dapter

import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.tisan.location.R
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.utils.CryptoUtil
import java.io.File
import java.io.FileInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

class ImagePreviewAdapter(private val images: List<EncryptedFileItem>) :
    RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {


    var onItemClick: ((EncryptedFileItem) -> Unit)? = null


    class ImageViewHolder(val imageView: PhotoView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val photoView = PhotoView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        return ImageViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = images[position]
        try {
//            val encBytes = File(item.filePath).readBytes()
//            val decrypted = CryptoUtil.decrypt(encBytes)
//            val bitmap = BitmapFactory.decodeByteArray(decrypted, 0, decrypted.size)


            val encryptedFile = File(item.filePath)
            val inputStream = FileInputStream(encryptedFile)
            val decryptedStream = CipherInputStream(inputStream, CryptoUtil.getCipher(Cipher.DECRYPT_MODE))
            val bitmap = BitmapFactory.decodeStream(decryptedStream)

            holder.imageView.setImageBitmap(bitmap)
            holder.imageView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        } catch (e: Exception) {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = images.size
}

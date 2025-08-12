package com.tisan.share.dapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tisan.location.R
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.utils.CryptoUtil
import java.io.File

class RecentAdapter : ListAdapter<EncryptedFileItem, RecentAdapter.RecentVH>(
    object : DiffUtil.ItemCallback<EncryptedFileItem>() {
        override fun areItemsTheSame(a: EncryptedFileItem, b: EncryptedFileItem) =
            a.fileName == b.fileName

        override fun areContentsTheSame(a: EncryptedFileItem, b: EncryptedFileItem) = a == b
    }
) {

    var lookLook: ((EncryptedFileItem) -> Unit)? = null


    override fun onCreateViewHolder(p: ViewGroup, vType: Int) =
        RecentVH(LayoutInflater.from(p.context).inflate(R.layout.item_recent_media, p, false))

    override fun onBindViewHolder(h: RecentVH, pos: Int) = h.bind(getItem(pos))

    inner class RecentVH(v: View) : RecyclerView.ViewHolder(v) {
        private val img = v.findViewById<ImageView>(R.id.imgThumb)
        private val play = v.findViewById<View>(R.id.icPlay)
        private val cvRecent = v.findViewById<CardView>(R.id.cv_recent)

        fun bind(item: EncryptedFileItem) {
            val bmp = loadThumb(item)
            if (bmp != null) {
                img.setImageBitmap(bmp)
                img.post {
                    val w = img.width
                    if (w > 0) {
                        img.layoutParams = img.layoutParams.apply {
                            height = (w * (bmp.height.toFloat() / bmp.width)).toInt()
                        }
                        img.requestLayout()
                    }
                }
            } else img.setImageResource(R.drawable.ic_placeholder_left)
            play.visibility = if (item.mimeType.startsWith("video/")) View.VISIBLE else View.GONE
            cvRecent.setOnClickListener {
                lookLook?.invoke(item)
            }
        }

        private fun loadThumb(it: EncryptedFileItem): Bitmap? {
            val p = it.thumbPath ?: return null
            return try {
                val enc = File(p).readBytes()
                val dec = CryptoUtil.decrypt(enc)        // uses your util
                BitmapFactory.decodeByteArray(dec, 0, dec.size)
            } catch (_: Exception) {
                null
            }
        }
    }
}

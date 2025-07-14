package com.tisan.share.dapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tisan.location.R
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.utils.CryptoUtil
import com.tisan.share.utils.LogUtil
import java.io.File

class ViewMoreFileAdapter(
    private val items: MutableList<EncryptedFileItem>
) : RecyclerView.Adapter<ViewMoreFileAdapter.ViewHolder>() {

    var onItemClick: ((EncryptedFileItem) -> Unit)? = null
    var onItemLongClick: (() -> Unit)? = null

    var isSelectionMode = false
    private val selectedItems = mutableSetOf<String>()

    fun isInSelectionMode(): Boolean = isSelectionMode

    fun enterSelectionMode(selectedItem: EncryptedFileItem) {
        isSelectionMode = true
        selectedItems.clear()
        selectedItems.add(selectedItem.filePath)
        notifyDataSetChanged()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun toggleSelectAll(selectAll: Boolean) {
        selectedItems.clear()
        if (selectAll) {
            items.forEach {
                selectedItems.add(it.filePath)
            }
        }
        notifyDataSetChanged()
    }

    fun isAllSelected(): Boolean = selectedItems.size == items.size

    fun getSelectedItems(): List<EncryptedFileItem> {
        return items.filter { selectedItems.contains(it.filePath) }
    }

    fun removeSelectedItems() {
        items.removeAll { selectedItems.contains(it.filePath) }
        selectedItems.clear()
        notifyDataSetChanged()
    }


    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_encrypted_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(item)
                notifyItemChanged(position)
            } else {
                onItemClick?.invoke(item)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                enterSelectionMode(item)
                onItemLongClick?.invoke()
            }
            true // 消费长按事件    6
        }
    }

    private fun toggleSelection(item: EncryptedFileItem) {
        if (selectedItems.contains(item.filePath)) {
            selectedItems.remove(item.filePath)
        } else {
            selectedItems.add(item.filePath)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val thumb = view.findViewById<ImageView>(R.id.fileThumb)
        private val name = view.findViewById<TextView>(R.id.fileName)
        private val checkBox = view.findViewById<android.widget.CheckBox>(R.id.cb)

        fun bind(item: EncryptedFileItem) {
            val timeStart = System.currentTimeMillis()
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
                    CryptoUtil.decryptFile(encryptedFile, tempFile)
                    // 用 Glide 加载解密后的图像
                    Glide.with(itemView.context)
                        .load(tempFile)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(thumb)

                } catch (e: Exception) {
                    // 加载失败时可以显示默认图
                    thumb.setImageResource(R.drawable.ic_launcher_background)
                }
            }

            checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            checkBox.isChecked = selectedItems.contains(item.filePath)

            checkBox.setOnClickListener {
                toggleSelection(item)
            }
            val timeEnd = System.currentTimeMillis()
            LogUtil.d("刷新一次时间","time:" + (timeEnd - timeStart))
        }
    }
}


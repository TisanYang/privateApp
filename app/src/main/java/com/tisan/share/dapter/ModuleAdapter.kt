package com.tisan.share.dapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tisan.location.R
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType

class ModuleAdapter : ListAdapter<FileModuleItem, ModuleAdapter.ModuleViewHolder>(DiffCallback()) {

    var onFileItemClick: ((EncryptedFileItem) -> Unit)? = null
    var onSeeMoreClick: ((FileModuleItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.moduleTitle)
        private val seeMore = view.findViewById<LinearLayout>(R.id.ll_seemore)
        private val fileRv = view.findViewById<RecyclerView>(R.id.fileRecyclerView)

        fun bind(item: FileModuleItem) {
            title.text = item.title
            seeMore.setOnClickListener {
                onSeeMoreClick?.invoke(item)
            }
            fileRv.layoutManager =
                LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            fileRv.adapter = EncryptedFileAdapter(item.files.take(6)).apply {// 最多显示6个预览
                // 👇 设置点击回调转发
                onItemClick = {
                    onFileItemClick?.invoke(it)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FileModuleItem>() {
        override fun areItemsTheSame(oldItem: FileModuleItem, newItem: FileModuleItem) =
            oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: FileModuleItem, newItem: FileModuleItem) =
            oldItem == newItem
    }
}

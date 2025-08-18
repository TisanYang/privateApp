package com.tisan.share.dapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kj.infinite.databinding.ItemAudioRecordBinding
import com.tisan.share.data.AudioRecordItem

class AudioRecordAdapter :
    ListAdapter<AudioRecordItem, AudioRecordAdapter.RecordViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<AudioRecordItem>() {
            override fun areItemsTheSame(old: AudioRecordItem, new: AudioRecordItem) =
                old.filePath == new.filePath

            override fun areContentsTheSame(old: AudioRecordItem, new: AudioRecordItem) =
                old == new
        }
    }

    inner class RecordViewHolder(val binding: ItemAudioRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioRecordItem) {
            binding.tvName.text = item.name
            binding.tvDuration.text = formatDuration(item.duration)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAudioRecordBinding.inflate(inflater, parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatDuration(ms: Long): String {
        val sec = ms / 1000
        val min = sec / 60
        val remainSec = sec % 60
        return String.format("%02d:%02d", min, remainSec)
    }
}

package com.tisan.share.feedback

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kj.infinite.databinding.ItemFeedbackBinding
import com.tisan.share.data.FeedbackRecord

class FeedbackAdapter(
    private val onFollowUpClick: (FeedbackRecord) -> Unit
) : ListAdapter<FeedbackRecord, FeedbackAdapter.FeedbackViewHolder>(DiffCallback()) {

    inner class FeedbackViewHolder(val binding: ItemFeedbackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedbackRecord) {
            binding.tvQuestion.text = item.question
            binding.tvQuestionTime.text = item.questionTime

            if (item.reply.isNullOrEmpty()) {
                binding.tvReply.text = "暂无回复"
                binding.tvReplyTime.text = ""
            } else {
                binding.tvReply.text = item.reply
                binding.tvReplyTime.text = item.replyTime
            }

            binding.btnFollowUp.setOnClickListener {
                onFollowUpClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FeedbackRecord>() {
        override fun areItemsTheSame(old: FeedbackRecord, new: FeedbackRecord) = old.id == new.id
        override fun areContentsTheSame(old: FeedbackRecord, new: FeedbackRecord) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

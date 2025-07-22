package com.tisan.share.data

data class FeedbackRecord(
    val id: String,
    val question: String,
    val questionTime: String,
    val reply: String?,
    val replyTime: String?
)

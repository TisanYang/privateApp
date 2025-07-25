package com.tisan.share.net

class request {
}

data class FeedbackRequest(
    val content: String
)

data class FollowUpRequest(
    val feedbackId: String,
    val content: String
)

data class FeedbackRecord(
    val id: String,
    val content: String,
    val reply: String?,
    val time: String
)

data class FeedbackItem(
    val id: String,
    val content: String,
    val reply: String?,
    val time: String
)

data class FeedbackSubmitRequest(
    val content: String
)



package com.tisan.share.net

import com.tisan.share.net.requestbean.CommentRequest
import com.tisan.share.net.responsebean.CommentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {

    @POST("/api/feedback/submit")
    suspend fun submitFeedback(@Body request: FeedbackSubmitRequest): ApiResponse<String>

    @GET("/api/feedback/list")
    suspend fun getFeedbackList(): ApiResponse<List<FeedbackRecord>>

    @POST("/api/feedback/followup")
    suspend fun submitFollowUp(@Body request: FollowUpRequest): ApiResponse<String>

    @POST("/api/add/comment")
    suspend fun addComment(@Body request: CommentRequest): ApiResponse<CommentResponse>

}



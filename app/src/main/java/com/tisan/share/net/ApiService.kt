package com.tisan.share.net

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
}



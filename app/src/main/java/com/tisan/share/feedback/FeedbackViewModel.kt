package com.tisan.share.feedback

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.base.UiState
import com.tisan.share.net.ApiResponse
import com.tisan.share.net.FeedbackSubmitRequest
import com.tisan.share.net.RetrofitClient
import com.tisan.share.net.requestbean.CommentRequest
import com.tisan.share.net.responsebean.CommentResponse
import com.tisan.share.utils.DeviceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedbackViewModel : BaseViewModel() {

    private val _submitResult = MutableLiveData<UiState<CommentResponse>>()
    val submitResult: LiveData<UiState<CommentResponse>?> = _submitResult

    val _submitResultInO = MutableLiveData<UiState<CommentResponse>>()
    val submitResultInO: LiveData<UiState<CommentResponse>> = _submitResultInO

    fun submitFeedbackInO(content: String, context: Context) {
        val request = CommentRequest(
            uuid = DeviceUtils.getDeviceUUID(context),
            model = Build.MODEL,
            content = content
        )

        // 使用封装好的 requestWithUiState
        requestWithUiState(_submitResultInO) {
            RetrofitClient.apiService.addComment(request) // ApiResponse<CommentResponse>
        }
    }

//    fun submitFeedbackInO(content: String, context: Context) {
//
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.apiService.addComment(
//                    CommentRequest(
//                        uuid = DeviceUtils.getDeviceUUID(context),
//                        model = Build.MODEL,
//                        content = content
//                    )
//                )
//                if (response.isSuccess()) {
//                    Log.d("API", "评论提交成功: ${response.data}")
//                    _submitResult.value = FeedbackUiState.Success(response.data)
//                } else {
//                    Log.e("API", "提交失败: ${response.message}")
//                    _submitResult.value =
//                        FeedbackUiState.Failure(response.message ?: "未知错误", response.code)
//                }
//            } catch (e: Exception) {
//                Log.e("API", "网络错误: ${e.message}")
//            }
//        }
//    }

    suspend fun submitFeedback(content: String, context: Context): ApiResponse<CommentResponse> {
        return RetrofitClient.apiService.addComment(
            CommentRequest(
                uuid = DeviceUtils.getDeviceUUID(context),
                model = Build.MODEL,
                content = content
            )
        )
    }
}


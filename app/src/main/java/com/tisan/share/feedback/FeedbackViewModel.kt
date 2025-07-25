package com.tisan.share.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.net.FeedbackSubmitRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedbackViewModel : BaseViewModel() {

    private val testData = MutableLiveData<String>()

    private val _submitResult = MutableLiveData<Boolean>()
    val submitResult: LiveData<Boolean> = _submitResult

    // 这里先用假提交模拟延时
    fun submitFeedback(content: String) {
        viewModelScope.launch {
            delay(1000) // 模拟网络延时
            // TODO: 这里接入接口
            _submitResult.value = true
        }
    }

    fun submitFeedback() {
        launchRequest(testData) { api.submitFeedback(FeedbackSubmitRequest("")) }
    }
}

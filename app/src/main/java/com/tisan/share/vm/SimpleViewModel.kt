package com.tisan.share.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tisan.share.base.BaseViewModel
import com.tisan.share.net.RetrofitClient
import com.tisan.share.net.requestbean.CommentRequest
import kotlinx.coroutines.launch

class SimpleViewModel : BaseViewModel() {
    fun submitComment() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.addComment(
                    CommentRequest(
                        uuid = "sjdjdjdjdjjjjjjhfgal",
                        model = "iphone16",
                        content = "大家好 这里是测试评论"
                    )
                )
                if (response.isSuccess()) {
                    Log.d("API", "评论提交成功: ${response.data}")
                } else {
                    Log.e("API", "提交失败: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("API", "网络错误: ${e.message}")
            }
        }
    }

}
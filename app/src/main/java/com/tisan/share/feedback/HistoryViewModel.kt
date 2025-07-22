package com.tisan.share.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tisan.share.base.BaseViewModel
import com.tisan.share.data.FeedbackRecord

class HistoryViewModel:BaseViewModel() {
    private val _records = MutableLiveData<List<FeedbackRecord>>()
    val records: LiveData<List<FeedbackRecord>> = _records

    init {
        loadFakeData()
    }

    private fun loadFakeData() {
        _records.value = listOf(
            FeedbackRecord(
                id = "1",
                question = "应用启动很慢怎么办？",
                questionTime = "2025-07-21 09:30",
                reply = "您好，请尝试清除缓存后重启应用。",
                replyTime = "2025-07-21 10:10"
            ),
            FeedbackRecord(
                id = "2",
                question = "反馈功能点了没反应。",
                questionTime = "2025-07-20 14:00",
                reply = null,
                replyTime = null
            ),
            FeedbackRecord(
                id = "3",
                question = "希望增加夜间模式功能。",
                questionTime = "2025-07-18 08:15",
                reply = "感谢您的建议，我们会在后续版本中考虑。",
                replyTime = "2025-07-18 09:00"
            )
        )
    }
}
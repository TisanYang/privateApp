package com.tisan.share.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tisan.share.base.BaseViewModel
import com.tisan.share.data.AudioRecordItem

class AudioVaultViewModel  constructor() : BaseViewModel() {

    private val allRecords = mutableListOf<AudioRecordItem>()

    private val _filteredRecords = MutableLiveData<List<AudioRecordItem>>()
    val filteredRecords: LiveData<List<AudioRecordItem>> = _filteredRecords

    fun loadAllRecords() {
        // TODO 替换为真实录音文件读取逻辑
        allRecords.clear()
        allRecords.addAll(listOf(
            AudioRecordItem("录音1", "/path/audio1.aac", 120_000L, System.currentTimeMillis()),
            AudioRecordItem("会议录音", "/path/audio2.aac", 90_000L, System.currentTimeMillis())
        ))
        _filteredRecords.value = allRecords
    }

    fun filter(query: String) {
        _filteredRecords.value = if (query.isBlank()) {
            allRecords
        } else {
            allRecords.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}

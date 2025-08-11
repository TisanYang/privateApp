package com.tisan.share.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewViewModel : ViewModel() {

    private val _url = MutableLiveData<String>()
    val url: LiveData<String> get() = _url

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    fun setUrl(url: String) {
        _url.value = url
    }

    fun setProgress(progress: Int) {
        _progress.value = progress
    }

    fun setLoading(loading: Boolean) {
        _loading.value = loading
    }

    fun setTitle(title: String) {
        _title.value = title
    }
}

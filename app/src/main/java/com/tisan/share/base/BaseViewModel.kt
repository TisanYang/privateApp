package com.tisan.share.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val errorMsg = MutableLiveData<String?>()
    val toastEvent = MutableLiveData<String>()

    /**
     * 快捷 log 打印
     */
    fun log(msg: String) {
        Log.d(this::class.java.simpleName, msg)
    }

    /**
     * 快捷弹 toast（通过 LiveData 通知 Fragment）
     */
    fun showToast(msg: String) {
        toastEvent.postValue(msg)
    }

    /**
     * 启动协程，支持自动切换线程、异常捕获、Loading控制
     */
    fun launch(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend CoroutineScope.() -> Unit,
        onError: (Throwable) -> Unit = { errorMsg.postValue(it.message) },
        onStart: () -> Unit = { isLoading.postValue(true) },
        onFinally: () -> Unit = { isLoading.postValue(false) }
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                onStart()
                block()
            } catch (e: Exception) {
                onError(e)
            } finally {
                onFinally()
            }
        }
    }

    /**
     * 快速切到主线程执行
     */
    suspend fun <T> onMain(block: suspend () -> T): T {
        return withContext(Dispatchers.Main) { block() }
    }

    /**
     * 快速切到 IO 线程执行
     */
    suspend fun <T> onIO(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) { block() }
    }
}


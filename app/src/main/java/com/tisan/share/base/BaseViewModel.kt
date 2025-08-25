package com.tisan.share.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tisan.share.net.ApiResponse
import com.tisan.share.net.ApiService
import com.tisan.share.net.RetrofitClient
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class BaseViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val errorMsg = MutableLiveData<String?>()
    val toastEvent = MutableLiveData<String>()

    val _uiState = MutableLiveData<UiState<Any>>()
    val uiState: LiveData<UiState<Any>> get() = _uiState

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .client(RetrofitClient.okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

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

    fun <T> launchWithUiState(
        onSuccess: (T?) -> Unit = {},
        onFailure: (String) -> Unit = {},
        block: suspend () -> ApiResponse<T>
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(3000)
            try {
                val result = block()
                if (result.isSuccess()) {
                    _uiState.value = UiState.Success(result.data)
                    onSuccess(result.data)
                } else {
                    _uiState.value = UiState.Failure(result.message)
                    onFailure(result.message)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "网络异常"
                _uiState.value = UiState.Failure(msg)
                onFailure(msg)
            }
        }
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

//    fun <T> request(
//        requestBlock: suspend () -> ApiResponse<T>,
//        resultLiveData: MutableLiveData<T>
//    ) {
//        viewModelScope.launch {
//            try {
//                isLoading.postValue(true)
//
//                val response = requestBlock()
//                if (response.isSuccess()) {
//                    response.data?.let {
//                        resultLiveData.postValue(it)
//                    }
//                } else {
//                    errorMsg.postValue(response.message)
//                }
//
//            } catch (e: Exception) {
//                errorMsg.postValue(e.message ?: "网络异常")
//            } finally {
//                isLoading.postValue(false)
//            }
//        }
//    }

    /**
     * 网络请求简化封装，无缓存版本
     */
    fun <T> launchRequest(
        resultLiveData: MutableLiveData<T>,
        block: suspend () -> ApiResponse<T>
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = block()
                if (response.isSuccess()) {
                    response.data?.let {
                        resultLiveData.postValue(it)
                    }
                } else {
                    errorMsg.postValue(response.message)
                }
            } catch (e: Exception) {
                errorMsg.postValue(e.message ?: "网络异常")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    protected fun <T> requestWithUiState(
        liveData: MutableLiveData<UiState<T>>,
        block: suspend () -> ApiResponse<T>
    ) {
        viewModelScope.launch {
            liveData.value = UiState.Loading
            delay(3000)
            try {
                val result = block()
                if (result.isSuccess()) {
                    liveData.value = UiState.Success(result.data)
                } else {
                    liveData.value = UiState.Failure(result.message)
                }
            } catch (e: Exception) {
                liveData.value = UiState.Failure(e.message ?: "网络异常")
            }
        }
    }

}

enum class CacheMode {
    NO_CACHE,
    ONLY_CACHE,
    ONLY_NETWORK,
    FIRST_CACHE_THEN_NETWORK
}


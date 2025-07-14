package com.tisan.share.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class SharedEventViewModel @Inject constructor() : ViewModel() {
    private val _refreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvent: SharedFlow<Unit> = _refreshEvent

    suspend fun sendRefreshEvent() {
        _refreshEvent.emit(Unit)
    }
}


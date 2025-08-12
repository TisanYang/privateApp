package com.tisan.share.utils

import kotlinx.coroutines.flow.MutableSharedFlow

// EventBus.kt
object EventBus {
    val fileListUpdated = MutableSharedFlow<Unit>(replay = 0)

    val updateCacheData = MutableSharedFlow<Unit>(replay = 0)

}

package com.tisan.share.net

object CacheManager {
    private val memoryCache = mutableMapOf<String, Any>()
    private const val DISK_CACHE_DIR = "net_cache"

    fun <T> getMemory(key: String): T? = memoryCache[key] as? T

    fun putMemory(key: String, value: Any) {
        memoryCache[key] = value
    }

    fun clearMemory() {
        memoryCache.clear()
    }

    // 可扩展磁盘缓存与过期控制
}

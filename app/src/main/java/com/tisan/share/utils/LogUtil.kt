package com.tisan.share.utils

object LogUtil {
    var enable = true

    private fun getStackTraceElement(): StackTraceElement? {
        val stackTrace = Thread.currentThread().stackTrace
        var foundLogUtil = false
        for (element in stackTrace) {
            val className = element.className
            if (className.contains("LogUtil")) {
                foundLogUtil = true
                continue
            }
            if (foundLogUtil) {
                return element
            }
        }
        return null
    }

    private fun getSimpleClassName(fullClassName: String): String {
        return fullClassName.substringAfterLast(".")
    }

    private fun getPageNameAndMethod(): Pair<String, String> {
        val ste = getStackTraceElement()
        val page = ste?.let { getSimpleClassName(it.className) } ?: "UnknownPage"
        val method = ste?.methodName ?: "UnknownMethod"
        return page to method
    }

    fun d(tag: String, msg: String) {
        if (!enable) return
        val (page, method) = getPageNameAndMethod()
        android.util.Log.d("$page::$method::$tag", msg)
    }

    fun i(tag: String, msg: String) {
        if (!enable) return
        val (page, method) = getPageNameAndMethod()
        android.util.Log.i("$page::$method::$tag", msg)
    }

    fun w(tag: String, msg: String) {
        if (!enable) return
        val (page, method) = getPageNameAndMethod()
        android.util.Log.w("$page::$method::$tag", msg)
    }

    fun e(tag: String, msg: String) {
        if (!enable) return
        val (page, method) = getPageNameAndMethod()
        android.util.Log.e("$page::$method::$tag", msg)
    }
}

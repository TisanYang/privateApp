package com.tisan.share.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceUtils {

    /**
     * 获取设备唯一标识（Android ID），通常卸载重装保持不变。
     * @param context 上下文
     * @return 设备唯一ID字符串，如果获取失败返回 "unknown_device_id"
     */
    fun getDeviceUUID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device_id"
    }



}

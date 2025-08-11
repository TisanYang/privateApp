package com.tisan.share

import android.app.Application
import com.tisan.share.utils.SecureSpHelper

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化加密 SharedPreferences
        SecureSpHelper.init(this)

    }
}

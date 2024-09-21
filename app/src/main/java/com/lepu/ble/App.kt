package com.lepu.ble

import android.app.Application
import com.lepu.ble.utils.LogUtil

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LogUtil.init(this)
    }
}

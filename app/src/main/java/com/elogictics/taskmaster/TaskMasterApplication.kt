package com.elogictics.taskmaster

import android.app.Application
import com.elogictics.taskmaster.utils.AndroidUtils
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class TaskMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferencesManager.init(this)
        SharedPreferencesManager.instance.putString(
            SharedPreferencesManager.DEVICE_ID, AndroidUtils.getAndroidDeviceId(applicationContext)
        )
    }
}
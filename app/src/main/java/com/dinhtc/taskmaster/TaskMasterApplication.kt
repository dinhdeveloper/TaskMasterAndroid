package com.dinhtc.taskmaster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
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
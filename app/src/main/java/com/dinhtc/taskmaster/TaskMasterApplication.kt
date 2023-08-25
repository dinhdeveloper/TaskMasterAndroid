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
        // Tạo kênh thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        Firebase.messaging.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MyFirebaseMsgService", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d("MyFirebaseMsgService", msg)
            },
        )
    }
}
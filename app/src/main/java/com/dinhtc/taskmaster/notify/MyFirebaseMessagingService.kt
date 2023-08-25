package com.dinhtc.taskmaster.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val notificationId = 1 // Đặt notificationId theo nhu cầu của bạn


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            // Xử lý thông báo từ dữ liệu
            sendNotification(remoteMessage.data)
        } else if (remoteMessage.notification != null) {
            // Xử lý thông báo từ notification
            sendNotificationFromNotification(remoteMessage.notification)
        }
    }

    private fun sendNotification(data: Map<String, String>) {
        val dataNotify = data["data"]
        val jsonObject = JSONObject(dataNotify)
        val notificationTitle = jsonObject.getString("title")?: "Chúc quý khách 1 ngày tốt lành"
        val notificationType = jsonObject.getString("type")?: "Default"
        val notificationBody = jsonObject.getString("data")?: "Chúc quý khách 1 ngày tốt lành"
//        val notificationTitle = data["title"] ?: "Default Title"
//        val notificationType = data["type"] ?: "Default Type"
//        val notificationBody = data["body"] ?: "Default Body"

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("fragmentToOpen", notificationType)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(R.drawable.icon_noti)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
        pushChangeIcon()
    }

    private fun sendNotificationFromNotification(notification: RemoteMessage.Notification?) {
        val notificationTitle = notification?.title ?: "Chúc quý khách 1 ngày tốt lành"
        val notificationBody = notification?.body ?: "Chúc quý khách 1 ngày tốt lành"

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(R.drawable.icon_noti)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
        pushChangeIcon()
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        //Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
        SharedPreferencesManager.instance.putString(SharedPreferencesManager.TOKEN_FIREBASE, token)
        AppEventBus.getInstance().publishEvent(EventBusAction.Action.REFRESH_TOKEN_FB)
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    private fun pushChangeIcon(){
        AppEventBus.getInstance().publishEvent(EventBusAction.Action.CHANGE_LOGO)
    }
}
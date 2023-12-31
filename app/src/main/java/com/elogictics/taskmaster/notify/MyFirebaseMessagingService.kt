package com.elogictics.taskmaster.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.utils.eventbus.AppEventBus
import com.elogictics.taskmaster.utils.eventbus.EventBusAction
import com.elogictics.taskmaster.view.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val autoInt = AtomicInteger(1)

    private val autoId: Int
        get() = autoInt.incrementAndGet()

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
        val notificationType = jsonObject.getString("type")?: "WORK"
        val notificationBody = jsonObject.getString("body")?: "Chúc quý khách 1 ngày tốt lành"
        val notificationData = jsonObject.getString("data")?: "Chúc quý khách 1 ngày tốt lành"

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("OPEN_FRAGMENT", notificationData)
        intent.putExtra("INFO_FRAGMENT", notificationType)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )

        createChannel(context = applicationContext, notificationTitle, notificationBody,pendingIntent)
    }

    private fun createChannel(
        context: Context,
        notificationTitle: String,
        notificationBody: String,
        pendingIntent: PendingIntent?
    ) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.icon_noti)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle(notificationTitle))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Cài đặt ưu tiên

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo một kênh thông báo (Notification Channel) cho Android Oreo trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(autoId, notificationBuilder.build())
    }



    private fun sendNotificationFromNotification(notification: RemoteMessage.Notification?) {
        val notificationTitle = notification?.title ?: "Chúc quý khách 1 ngày tốt lành"
        val notificationBody = notification?.body ?: "Chúc quý khách 1 ngày tốt lành"

        createChannel(context = applicationContext, notificationTitle, notificationBody,null)
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
}
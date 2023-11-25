package com.cmpt362.blissful.ui.social

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.cmpt362.blissful.MainActivity
import com.cmpt362.blissful.ui.home.HomeFragment


class NotifyService : Service(){
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 111
    private val CHANNEL_ID = "notification channel"
    override fun onCreate() {
        Log.d("debug", "Service onCreate() called")
        super.onCreate()
        showNotification()



    }
    private fun showNotification() {
        Log.d("debug", "Service showNotification() called")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("frgToLoad", 1);
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setSmallIcon(R.drawable.ic_delete)
        notificationBuilder.setContentTitle("Did you enter your gratitude today?")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(false)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
        println("debug: Service onDestroy")

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        Log.d("debug", "Service onBind() called")
        return null
    }

}

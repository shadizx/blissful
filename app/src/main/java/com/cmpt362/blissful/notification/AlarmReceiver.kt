package com.cmpt362.blissful.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cmpt362.blissful.MainActivity
import com.cmpt362.blissful.R

class AlarmReceiver : BroadcastReceiver() {

    // Global constants of Notification channel & IDs
    companion object {
        const val CHANNEL_NAME = "Blissful Notification Channel"
        const val CHANNEL_ID = "Blissful Notification Channel ID"
        const val NOTIFICATION_ID = 114514
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        notify(context)
    }

    private fun notify(context: Context) {

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Blissful")
            .setContentText("Find something blissful today? Come and share it with other grateful people!")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        val channelName = CHANNEL_NAME
        val descriptionText = "Notification Channel of Blissful App"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
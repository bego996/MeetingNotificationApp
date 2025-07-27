package com.example.meetingnotification.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.meetingnotification.ui.MainActivity
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.contact.BeforeTemplateDestination

object NotificationHelper {

    private const val CHANNEL_ID = "weekly_reminder_channel"
    private const val CHANNEL_NAME = "Wöchentliche Erinnerungen"
    private const val NOTIFICATION_ID = 101


    fun showWeeklyReminder(context: Context, count: Int) {
        createChannel(context)

        val intent = Intent(context,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination",BeforeTemplateDestination.route)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_email_24) // Stelle sicher, dass du ein Icon hast
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text,count))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }


    private fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
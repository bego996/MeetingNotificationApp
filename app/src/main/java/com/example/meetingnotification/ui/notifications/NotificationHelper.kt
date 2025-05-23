package com.example.meetingnotification.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.meetingnotification.ui.R

object NotificationHelper {

    private const val CHANNEL_ID = "weekly_reminder_channel"
    private const val CHANNEL_NAME = "Wöchentliche Erinnerungen"
    private const val NOTIFICATION_ID = 101


    fun showWeeklyReminder(context: Context, count: Int) {
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.calendar_icon_no_orange) // Stelle sicher, dass du ein Icon hast
            .setContentTitle("Erinnerungen für diese Woche")
            .setContentText("Du kannst $count Kontakte in dieser Woche erinnern.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
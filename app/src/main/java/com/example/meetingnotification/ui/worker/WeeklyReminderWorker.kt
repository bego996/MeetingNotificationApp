package com.example.meetingnotification.ui.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.meetingnotification.ui.data.ContactDatabase
import com.example.meetingnotification.ui.notifications.NotificationHelper
import java.time.LocalDate

class WeeklyReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val dao = ContactDatabase.getDatabase(applicationContext).eventDAO()

        val today = LocalDate.now().toString()
        val endOfWeek = LocalDate.parse(today).plusDays(7).toString()

        val eventCountForThisWeek = dao.getNotNotifiedEventsAndFromActualDateTime(
            today,
            endOfWeek
        )

        NotificationHelper.showWeeklyReminder(applicationContext, eventCountForThisWeek)
        return Result.success()
    }
}
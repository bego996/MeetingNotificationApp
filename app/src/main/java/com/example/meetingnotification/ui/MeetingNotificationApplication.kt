package com.example.meetingnotification.ui

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meetingnotification.ui.data.AppContainer
import com.example.meetingnotification.ui.data.AppDataContainer
import com.example.meetingnotification.ui.data.repositories.BackgroundImageManagerRepository
import com.example.meetingnotification.ui.data.repositories.InstructionReadRepository
import com.example.meetingnotification.ui.worker.MonthlyEventDbCleaner
import com.example.meetingnotification.ui.worker.WeeklyReminderWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private val TAG = MeetingNotificationApplication::class.simpleName

class  MeetingNotificationApplication :Application() {

    lateinit var container: AppContainer
    lateinit var backgroundImageRepository: BackgroundImageManagerRepository
    lateinit var instructionReadStateRepository: InstructionReadRepository

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        Log.d(TAG,"AppContainerCreated() in MeetingNotificationApplication.")
        backgroundImageRepository = BackgroundImageManagerRepository(this)
        instructionReadStateRepository = InstructionReadRepository(this)

        scheduleWeeklyReminder()    //Worker Registration for BackgroundNotification.
        Log.d(TAG,"Weekly Notification Reminder registered()")
        sheduleMonthlyDatabaseCleaner() //Worker registration for event db cleaner.
        Log.d(TAG,"Monthly Database Cleaner for expired Events registered()")
    }


    //region Background Notification Helper
    private fun scheduleWeeklyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(16, TimeUnit.MINUTES)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weeklyReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = LocalDateTime.now()
        val nextRun = now.plusMinutes(5)

        val delay = Duration.between(now, nextRun)
        return delay.toMillis()
    }
    //endregion

    //region Monthly Event in Database Cleaner.
    private fun sheduleMonthlyDatabaseCleaner(){
        val workRequest = PeriodicWorkRequestBuilder<MonthlyEventDbCleaner>(
            16,TimeUnit.MINUTES)
            .setConstraints(
            Constraints.Builder()
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup_old_events",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    //endregion
}
package com.example.meetingnotification.ui

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meetingnotification.ui.broadcastReceiver.WeeklyAlarmReceiver
import com.example.meetingnotification.ui.broadcastReceiver.WeeklyEventDbUpdater
import com.example.meetingnotification.ui.data.AppContainer
import com.example.meetingnotification.ui.data.AppDataContainer
import com.example.meetingnotification.ui.data.repositories.BackgroundImageManagerRepository
import com.example.meetingnotification.ui.data.repositories.InstructionReadRepository
import com.example.meetingnotification.ui.worker.MonthlyEventDbCleaner
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.Calendar
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

        sheduleWeeklyAlarm()
        Log.d(TAG,"Weekly Notification Reminder registered()")

        sheduleMonthlyDatabaseCleaner() //Worker registration for event db cleaner.
        Log.d(TAG,"Monthly Database Cleaner for expired Events registered()")

        sheduleWeeklyEventUpdate()
        Log.d(TAG,"Weekly event db updater registered()")
    }

    //region Weekly background event updater. With Alarm Manager.
    private fun sheduleWeeklyEventUpdate(){
        Log.d(TAG,"sheduleWeeklyEventUpdate called()")
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, WeeklyEventDbUpdater::class.java)
        intent.action = "SET_ALARM_FOR_EVENT_DB_UPDATER"

        val nextIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) // Oder beliebigen Tag
            set(Calendar.HOUR_OF_DAY, 21) // Deine gewünschte Uhrzeit
            set(Calendar.MINUTE, 20)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 7)
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            nextIntent
        )

    }
    //endregion

    //region Weekly notification to inform about notifyable contacts at exact time with Alarm Manager.
    private fun sheduleWeeklyAlarm() {
        Log.d(TAG,"sheduleWeeklyAlarm called()")
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, WeeklyAlarmReceiver::class.java)
        intent.action = "ALARM_SET_AFTER_BOOT_OR_ON_FIRST_START"
        val nextIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) // Oder beliebigen Tag
            set(Calendar.HOUR_OF_DAY, 21) // Deine gewünschte Uhrzeit
            set(Calendar.MINUTE, 15)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 7)
        }

        //Shedule new Alarm for weekly notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    nextIntent
                )
                Log.d(TAG,"Alarm set for Notification and permission granted.")
                FirebaseCrashlytics.getInstance().log("Alarm set for Notification and permission granted.")
            } else {
                Log.d(TAG,"No permissions granted for WeeklyAlarmNotification in BroadcastReceiver, normal Alarm will be initiated!")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    nextIntent
                )
            }
        }else{
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                nextIntent
            )
        }
    }
    //endregion

    //region Monthly Event in Database Cleaner.
    private fun sheduleMonthlyDatabaseCleaner(){
        val workRequest = PeriodicWorkRequestBuilder<MonthlyEventDbCleaner>(30,TimeUnit.DAYS).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup_old_events",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    //endregion
}
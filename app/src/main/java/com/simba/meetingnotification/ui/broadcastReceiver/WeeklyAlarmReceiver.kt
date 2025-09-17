package com.simba.meetingnotification.ui.broadcastReceiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simba.meetingnotification.ui.data.ContactDatabase
import com.simba.meetingnotification.ui.notifications.NotificationHelper
import com.simba.meetingnotification.ui.utils.DebugUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

private val TAG = WeeklyAlarmReceiver::class.simpleName // Deklariert eine private Konstante `TAG` mit dem Namen der Klasse zur Verwendung in Log-Nachrichten

class WeeklyAlarmReceiver : BroadcastReceiver() {

    //This method is called to register a new Alarm and to show a weekly notification.
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "ALARM_SET_AFTER_BOOT_OR_ON_FIRST_START") {

            Log.d(TAG, "WeeklyAlarm Receiver triggered()")
            FirebaseCrashlytics.getInstance().log("WeeklyAlarm Receiver triggered()")

            CoroutineScope(Dispatchers.IO).launch {
                DebugUtils.logExecutionTime(TAG,"weekly_alarm_receiver") {
                    try {
                        val dao = ContactDatabase.getDatabase(context).eventDAO()
                        val today = LocalDate.now().toString()
                        val endOfWeek = LocalDate.parse(today).plusDays(7).toString()

                        val eventCountForThisWeek = dao.getNotNotifiedEventsAndFromActualDateTime(today, endOfWeek)

                        NotificationHelper.showWeeklyReminder(context, eventCountForThisWeek)

                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val nextIntent = Intent(context, WeeklyAlarmReceiver::class.java).setAction("ALARM_SET_AFTER_BOOT_OR_ON_FIRST_START")
                        val nextPendingIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // Oder beliebigen Tag
                            set(Calendar.HOUR_OF_DAY,12) // Deine gewünschte Uhrzeit
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            if (before(Calendar.getInstance())) add(Calendar.DATE, 7)
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    nextPendingIntent
                                )
                                Log.d(TAG, "ExactAlarmShedule set for Notification and permission granted.")
                                FirebaseCrashlytics.getInstance().log("ExactAlarmShedule set for Notification and permission granted.")
                            } else {
                                alarmManager.setAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    nextPendingIntent
                                )
                                Log.d(TAG, "No permissions granted for WeeklyAlarmNotification in BroadcastReceiver, normal Alarm will be initiated!")
                                FirebaseCrashlytics.getInstance().log("No permissions granted for WeeklyAlarmNotification in BroadcastReceiver, normal Alarm will be initiated!")
                            }
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                nextPendingIntent
                            )
                            Log.d(TAG, "ExaxtAlarmShedule set for Notification and permission dont needed because api < 33.")
                            FirebaseCrashlytics.getInstance().log("ExactAlarmShedule set for Notification and permission dont needed because api < 33.")
                        }
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().log("CoroutineScope failed!")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Log.e(TAG, "CoroutineScope failed!", e)
                    }
                }
            }
        }
    }
}
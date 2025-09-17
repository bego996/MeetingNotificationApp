package com.simba.meetingnotification.ui.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simba.meetingnotification.ui.data.ContactDatabase
import java.time.LocalDate

private val TAG = MonthlyEventDbCleaner::class.simpleName

//This is an coroutine worker which can execute operations anynchronously. Used to clean the DB from old Events monthly.
class MonthlyEventDbCleaner(context: Context, workerParams:WorkerParameters) : CoroutineWorker(context,workerParams) {

    override suspend fun doWork(): Result {
        val dao = ContactDatabase.getDatabase(applicationContext).eventDAO()
        val today = LocalDate.now().toString()
        val expiredEvents = dao.getExpiredEvents(today)

        Log.d(TAG,"Events:\n$expiredEvents")

        if (expiredEvents.isNotEmpty()){
            dao.deleteExpiredEvents(expiredEvents)
        }
        return Result.success()
    }
}
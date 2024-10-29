package com.example.meetingnotification.ui

import android.app.Application
import android.util.Log
import com.example.meetingnotification.ui.data.AppContainer
import com.example.meetingnotification.ui.data.AppDataContainer

private val TAG = MeetingNotificationApplication::class.simpleName

class  MeetingNotificationApplication :Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        Log.d(TAG,"AppContainerCreated() in MeetingNotificationApplication.")
    }
}
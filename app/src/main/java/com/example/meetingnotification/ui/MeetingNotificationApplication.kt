package com.example.meetingnotification.ui

import android.app.Application
import com.example.meetingnotification.ui.data.AppContainer
import com.example.meetingnotification.ui.data.AppDataContainer

class  MeetingNotificationApplication :Application() {


    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
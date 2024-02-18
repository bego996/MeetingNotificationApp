package com.example.meetingnotification.ui

import android.app.Application
import com.example.meetingnotification.ui.data.AppContainer
import com.example.meetingnotification.ui.data.AppDataContainer
import com.example.meetingnotification.ui.data.ContactRepository
import com.example.meetingnotification.ui.data.ContactRepositoryImpl

class  MeetingNotificationApplication :Application() {

    val myRepository: ContactRepository by lazy {
        ContactRepositoryImpl()
    }

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
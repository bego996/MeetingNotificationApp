package com.example.meetingnotification.ui.Services

import com.example.meetingnotification.ui.contact.ContactReadyForSms

interface SmsSendingServiceInteractor {
    fun performServiceAction(action : ServiceAction,vararg contacts: List<ContactReadyForSms>)
}
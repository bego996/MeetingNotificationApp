package com.example.meetingnotification.ui.services

import com.example.meetingnotification.ui.contact.ContactReadyForSms

interface SmsSendingServiceInteractor {
    fun performServiceAction(action : ServiceAction, vararg contacts: List<ContactReadyForSms>)
}
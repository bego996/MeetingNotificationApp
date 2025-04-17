package com.example.meetingnotification.ui.services

import com.example.meetingnotification.ui.contact.ContactReadyForSms

interface SmsSendingServiceInteractor {
    fun performServiceActionToAddOrSend(action : ServiceAction, contacts: List<ContactReadyForSms>)
    fun performServiceActionToGetContactFromQueue(action: ServiceAction) : List<Int>
    fun performServiceActionToRemoveFromQueue(action: ServiceAction, contactIds: List<Int>)
}
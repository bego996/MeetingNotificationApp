package com.simba.meetingnotification.ui.services

import com.simba.meetingnotification.ui.contact.ContactReadyForSms

interface SmsSendingServiceInteractor {
    fun performServiceActionToAddOrSend(action : ServiceAction, contacts: List<ContactReadyForSms>)
    fun performServiceActionToGetContactFromQueue(action: ServiceAction) : List<Int>
    fun performServiceActionToRemoveFromQueue(action: ServiceAction, contactId:Int)
}
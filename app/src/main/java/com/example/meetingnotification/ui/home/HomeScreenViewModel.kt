package com.example.meetingnotification.ui.home


import androidx.lifecycle.ViewModel
import com.example.meetingnotification.ui.data.ContactRepository
import com.example.meetingnotification.ui.data.EventRepository


class HomeScreenViewModel(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    /*
        suspend fun addContactToDatabase(contact: Contact){
            contactRepository.insertItem(contact)
        }
     */
}
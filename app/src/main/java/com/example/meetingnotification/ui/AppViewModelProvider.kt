package com.example.meetingnotification.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.home.HomeScreenViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            HomeScreenViewModel(
                inventoryApplication().container.contactRepository
            )
        }
        initializer {
            ContactsSearchScreenViewModel(
                inventoryApplication().container.contactRepository,
                this.createSavedStateHandle()
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): MeetingNotificationApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MeetingNotificationApplication)
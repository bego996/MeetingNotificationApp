package com.example.meetingnotification.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.contact.ContactCheckBeforeSubmitViewModel
import com.example.meetingnotification.ui.contact.ContactsScreenViewModel
import com.example.meetingnotification.ui.home.HomeScreenViewModel


//Object um ein viewmodel factory provider zu nutzen.
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeScreenViewModel(
                inventoryApplication().container.contactRepository
            )
        }
        initializer {
            ContactsSearchScreenViewModel(
                inventoryApplication().container.contactRepository
                //this.createSavedStateHandle()
            )
        }
        initializer {
            ContactsScreenViewModel(
                inventoryApplication().container.contactRepository
            )
        }
        initializer {
            ContactCheckBeforeSubmitViewModel(
                inventoryApplication().container.contactRepository
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): MeetingNotificationApplication = // Funktion, um die Anwendung aus `CreationExtras` zu holen
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MeetingNotificationApplication) // Holt den Anwendungs-Context und gibt ihn als `MeetingNotificationApplication` zurück
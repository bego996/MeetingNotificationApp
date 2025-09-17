package com.simba.meetingnotification.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.simba.meetingnotification.ui.contact.ContactCheckBeforeSubmitViewModel
import com.simba.meetingnotification.ui.contact.ContactsScreenViewModel
import com.simba.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.simba.meetingnotification.ui.home.HomeScreenViewModel
import com.simba.meetingnotification.ui.home.InstructionsScreenViewModel


//Object um ein viewmodel factory provider zu nutzen. Wird gebraucht um Viewmodel factories erstellen zu können und um ihnen die DB Repositories bereit zu stellen.
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeScreenViewModel(
                inventoryApplication().container.dateMessageSendRepository,
                inventoryApplication().resources,
                inventoryApplication().backgroundImageRepository
            )
        }
        initializer {
            ContactsSearchScreenViewModel(
                inventoryApplication().container.contactRepository,
                inventoryApplication().backgroundImageRepository,
                inventoryApplication().resources
            )
        }
        initializer {
            ContactsScreenViewModel(
                inventoryApplication().container.contactRepository,
                inventoryApplication().backgroundImageRepository
            )
        }
        initializer {
            ContactCheckBeforeSubmitViewModel(
                inventoryApplication().container.contactRepository,
                inventoryApplication().container.eventRepository,
                inventoryApplication().backgroundImageRepository
            )
        }
        initializer {
            InstructionsScreenViewModel(
                inventoryApplication().instructionReadStateRepository
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): MeetingNotificationApplication = // Funktion, um die Anwendung aus `CreationExtras` zu holen
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MeetingNotificationApplication) // Holt den Anwendungs-Context und gibt ihn als `MeetingNotificationApplication` zurück
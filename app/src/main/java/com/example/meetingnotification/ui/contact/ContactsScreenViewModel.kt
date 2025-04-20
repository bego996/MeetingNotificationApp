package com.example.meetingnotification.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsScreenViewModel(                             // ViewModel zur Verwaltung der Kontakte in einer UI
    private val contactRepository: ContactRepository,       // Repository, das die Datenquelle für die Kontakte darstellt
    private val eventRepository: EventRepository
) : ViewModel() {

    val contactsUiState: StateFlow<ContactUiState> =       // StateFlow zur Bereitstellung des Kontaktzustands in der UI
        contactRepository.getAllContactsStream().map { ContactUiState(it) } // Holt alle Kontakte aus dem Repository und wandelt sie in ein UI-Format um
            .stateIn(
                scope = viewModelScope,                     // Coroutine-Scope des ViewModels für Nebenläufigkeit
                started = SharingStarted.WhileSubscribed(5_000L), // Teilt Daten für 5 Sekunden nach dem Abbestellen weiter
                initialValue = ContactUiState()             // Initialwert des StateFlow ist ein leerer Zustand
            )

    fun deleteContact(contact: Contact) {          // Kein supsend nötig weil courtinescope innerhalb ausgeführt wird die sowieso asynchron ist.
        viewModelScope.launch {                            // Startet eine neue Coroutine im Bereich des ViewModels
            contactRepository.deleteItem(contact)          // Löscht den angegebenen Kontakt aus dem Repository (Database)
        }
    }

}

data class ContactUiState(val contactUiState: List<Contact> = listOf()) // Datenklasse zur Darstellung des UI-Zustands mit einer leeren Liste als Standard


package com.example.meetingnotification.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsScreenViewModel(
    private val contactRepository: ContactRepository
) : ViewModel(){

    val contactsUiState: StateFlow<ContactUiState> =
        contactRepository.getAllContactsStream().map { ContactUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ContactUiState()
            )


    suspend fun deleteContact(contact: Contact){
        viewModelScope.launch {
            contactRepository.deleteItem(contact)
        }
    }



}
data class ContactUiState(val contactUiState: List<Contact> = listOf())

package com.example.meetingnotification.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ContactCheckBeforeSubmitViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    val contactUiState : StateFlow<ContactsUiState3> =
        repository.getAllContactsStream().map { ContactsUiState3(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ContactsUiState3()
            )






}

data class ContactsUiState3(val contactUiState: List<Contact> = listOf())
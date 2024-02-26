package com.example.meetingnotification.ui.contact

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ContactCheckBeforeSubmitViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    val contactUiState: StateFlow<ContactsUiState3> =
        repository.getAllContactsStream().map { ContactsUiState3(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ContactsUiState3()
            )


    private val _calenderState = MutableStateFlow<List<EventDateTitle>>(emptyList())

    private val calenderState: StateFlow<List<EventDateTitle>> = _calenderState

    private val _calenderStateConnectedToContacts = mutableStateOf<List<ContactZipedWithDate>>(emptyList())

    val calenderStateConnectedToContacts: State<List<ContactZipedWithDate>>  = _calenderStateConnectedToContacts


    suspend fun updateContact(contact: Contact) {
        repository.updateItem(contact)
    }

    fun loadCalenderData(events: List<EventDateTitle>) {
        _calenderState.value = events
    }

    fun getCalenderState(): StateFlow<List<EventDateTitle>> = calenderState

    fun getDayDuration(meetingDate: String) : String{
        val meetingDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val daysBeetweenNowAndMeetingDate = LocalDate.now().until(LocalDate.parse(meetingDate,meetingDateFormat)).days
        return "$daysBeetweenNowAndMeetingDate Days Left"
    }


    //2024-03-17T12:30
    fun zipDatesToContacts(contacts: List<Contact>) {
        Log.d("Loger", "Launched")
        val dates = calenderState.value
        val listZiped = mutableListOf<ContactZipedWithDate>()
        val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")


        for (contact in contacts) {
            dates.firstOrNull {
                it.eventName.split(" ")[0] == contact.firstName.split(" ")[0] && it.eventName.split(" ")[1] == contact.lastName }?.let { date ->
                listZiped.add(
                    ContactZipedWithDate(
                        contact.id,
                        date.eventDate.toLocalDate().format(outputFormatterDate),
                        date.eventDate.toLocalTime().toString()
                    )
                )
            }
        }
        _calenderStateConnectedToContacts.value = listZiped

    }


}

data class ContactZipedWithDate(val contactId: Int, val date: String,val time: String)
data class MutablePairs2(var first: Int, var second: Boolean)
data class ContactsUiState3(val contactUiState: List<Contact> = listOf())
package com.example.meetingnotification.ui.contact

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.Services.SmsSendingService
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _calenderStateConnectedToContacts =
        mutableStateOf<List<ContactZippedWithDate>>(emptyList())

    val calenderStateConnectedToContacts: State<List<ContactZippedWithDate>> =
        _calenderStateConnectedToContacts


    private var smsService = listOf<SmsSendingService>()

    fun addSmsServiceInList(service: SmsSendingService){
        smsService = listOf(service)
    }

    fun getSmsService() : SmsSendingService = smsService[0]
    suspend fun updateContact(contact: Contact) {
        repository.updateItem(contact)
    }

    fun loadCalenderData(events: List<EventDateTitle>) {
        _calenderState.value = events
    }

    private fun getCalenderState(): List<EventDateTitle> = calenderState.value

    fun getDayDuration(meetingDate: String): String {
        val meetingDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val daysBeetweenNowAndMeetingDate =
            LocalDate.now().until(LocalDate.parse(meetingDate, meetingDateFormat)).days
        return "$daysBeetweenNowAndMeetingDate Days Left"
    }


    //2024-03-17T12:30
    fun zipDatesToContacts(contacts: List<Contact>) {
        val dates = getCalenderState()
        val listZipped = mutableListOf<ContactZippedWithDate>()
        val outputFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        for (contact in contacts) {
            dates.firstOrNull {
                it.eventName.split(" ")[0] == contact.firstName.split(" ")[0] && it.eventName.split(
                    " "
                )[1] == contact.lastName
            }?.let { date ->
                listZipped.add(
                    ContactZippedWithDate(
                        contact.id,
                        date.eventDate.toLocalDate().format(outputFormatterDate),
                        date.eventDate.toLocalTime().toString()
                    )
                )
            }
        }
        _calenderStateConnectedToContacts.value = listZipped
        updateContactsMessageAfterZippingItWithDates(listZipped,contacts)
    }

    fun updateContactsMessageAfterZippingItWithDates(zippedDateToContacts : MutableList<ContactZippedWithDate>,contactList : List<Contact>){
        zippedDateToContacts.isNotEmpty().let {
            viewModelScope.launch {
                for (zipValue in zippedDateToContacts) {
                    contactList.firstOrNull { it.id == zipValue.contactId }?.let { contact ->
                        repository.updateItem(
                            Contact(
                                id = contact.id,
                                title = contact.title,
                                firstName = contact.firstName,
                                lastName = contact.lastName,
                                sex = contact.sex,
                                phone = contact.phone,
                                message = updateMessageWithCorrectDateTime(contact.message,zipValue.date,zipValue.time)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun updateMessageWithCorrectDateTime(originMessage: String, dateReplacement: String, timeReplacement: String): String {
    return originMessage.replace("dd.MM.yyyy", dateReplacement).replace("HH:mm", timeReplacement)
}

data class ContactZippedWithDate(val contactId: Int, val date: String, val time: String)
data class MutablePairs2(var first: Int, var second: Boolean)
data class ContactsUiState3(val contactUiState: List<Contact> = listOf())
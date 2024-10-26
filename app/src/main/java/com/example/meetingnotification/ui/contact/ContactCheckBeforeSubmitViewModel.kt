package com.example.meetingnotification.ui.contact

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ContactCheckBeforeSubmitViewModel(
    private val contactRepository: ContactRepository,                // Repository, das zur Datenverwaltung verwendet wird
    private val eventRepository: EventRepository
) : ViewModel() {

    // Ein StateFlow-Objekt, das den aktuellen Zustand der Kontakte verwaltet
    val contactUiState: StateFlow<ContactsUiState3> =
        contactRepository.getAllContactsStream().map { ContactsUiState3(it) }
            .stateIn(
                scope = viewModelScope,                       // Coroutine-Bereich für Nebenläufigkeit
                started = SharingStarted.WhileSubscribed(5_000L), // Teile die Daten, solange abonniert, mit einer Verzögerung von 5000 ms
                initialValue = ContactsUiState3()             // Anfangswert des StateFlow
            )



    private val _calenderState = MutableStateFlow<List<EventDateTitle>>(emptyList())    // MutableStateFlow zur Verwaltung der Kalenderdaten
    private val calenderState: StateFlow<List<EventDateTitle>> = _calenderState         // Unveränderlicher StateFlow zur Abfrage der Kalenderdaten

    private val _calenderStateConnectedToContacts = mutableStateOf<List<ContactZippedWithDate>>(emptyList())            // MutableState zur Verknüpfung von Kontakten mit Kalenderdaten
    val calenderStateConnectedToContacts: State<List<ContactZippedWithDate>> = _calenderStateConnectedToContacts        // Öffentlicher Zugriff auf die verknüpften Kalenderdaten

    private var contactListReadyForSms by mutableStateOf(listOf<ContactReadyForSms>())      // Kontakte, die bereit für den SMS-Versand sind

    fun getContactsReadyForSms(): List<ContactReadyForSms> = contactListReadyForSms         // Gibt die Liste der Kontakte für den SMS-Versand zurück


    fun updateListReadyForSms(contacts: List<ContactReadyForSms>) {    // Aktualisiert die Liste der Kontakte für den SMS-Versand
        contactListReadyForSms = contacts
    }


    fun updateContact(contact: Contact) { // Aktualisiert einen bestimmten Kontakt im Repository. kein supend nötig weil courtinescope unten ausgeführt wird.
        viewModelScope.launch {
            contactRepository.updateItem(contact)
        }
    }

    private fun insertEventForContact(contactZippedWithDate: List<ContactZippedWithDate>){
        viewModelScope.launch {
            contactZippedWithDate.forEach{
                eventRepository.insertItem(Event(eventDate = it.date, eventTime = it.time, contactOwnerId = it.contactId))
            }
        }
    }

    fun loadCalenderData(events: List<EventDateTitle>) {               // Lädt die Kalenderdaten in das MutableStateFlow
        _calenderState.value = events
    }

    private fun getCalenderState(): List<EventDateTitle> = calenderState.value      // Gibt die aktuelle Liste der Kalenderereignisse zurück

    fun getDayDuration(meetingDate: String): String {        // Berechnet die Anzahl der Tage bis zum angegebenen Datum
        val meetingDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy") // Datumsformat für die Berechnung
        val daysBeetweenNowAndMeetingDate =
            LocalDate.now().until(
                LocalDate.parse(
                    meetingDate,
                    meetingDateFormat
                )
            ).days // Tage bis zum Datum berechnen
        return "$daysBeetweenNowAndMeetingDate Days Left"    // Gibt die verbleibenden Tage als Zeichenkette zurück
    }

    fun zipDatesToContacts(contacts: List<Contact>) {                              // Verknüpft Kontakte mit Kalenderdaten
        val dates = getCalenderState()                                             // Holt die aktuelle Liste der Kalenderereignisse
        val listZipped = mutableListOf<ContactZippedWithDate>()                    // Eine Liste zur Speicherung der verknüpften Daten
        val outputFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy") // Ausgabeformat für das Datum

        for (contact in contacts) {
            dates.firstOrNull {
                it.eventName.split(" ")[0] == contact.firstName.split(" ")[0] && it.eventName.split(
                    " ")[1] == contact.lastName                       // Prüft, ob ein Kalenderereignis zum Kontakt passt
            }?.let { date ->                                   // Wenn ein passendes Ereignis gefunden wird
                    listZipped.add(
                        ContactZippedWithDate(
                            contact.id,
                            date.eventDate.toLocalDate()
                                .format(outputFormatterDate),          // Formatiertes Datum
                            date.eventDate.toLocalTime().toString()    // Uhrzeit als Zeichenkette
                        )
                    )
                }
        }
        _calenderStateConnectedToContacts.value = listZipped           // Aktualisiert die MutableState-Liste mit den verknüpften Daten
        insertEventForContact(listZipped)
        updateContactsMessageAfterZippingItWithDates(
            listZipped,
            contacts
        )   // Aktualisiert die Nachrichten der Kontakte nach der Verknüpfung
    }


    private fun updateContactsMessageAfterZippingItWithDates(zippedDateToContacts: MutableList<ContactZippedWithDate>, contactList: List<Contact>) {                             // Aktualisiert die Nachrichten der Kontakte
        zippedDateToContacts.isNotEmpty().let {                  // Nur wenn verknüpfte Daten vorhanden sind
                viewModelScope.launch {                           // Startet eine neue Coroutine
                    for (zipValue in zippedDateToContacts) {      // Durchläuft die Liste der verknüpften Daten
                        contactList.firstOrNull { it.id == zipValue.contactId }?.let { contact ->                    // Sucht den passenden Kontakt
                                contactRepository.updateItem(            // Aktualisiert den Kontakt mit der neuen Nachricht
                                    Contact(
                                        id = contact.id,
                                        title = contact.title,
                                        firstName = contact.firstName,
                                        lastName = contact.lastName,
                                        sex = contact.sex,
                                        phone = contact.phone,
                                        message = updateMessageWithCorrectDateTime(
                                            contact.message,
                                            zipValue.date,
                                            zipValue.time
                                        )                         // Aktualisiert die Nachricht
                                    )
                                )
                            }
                    }
                }
            }
    }
}


private fun updateMessageWithCorrectDateTime(originMessage: String, dateReplacement: String, timeReplacement: String): String { // Ersetzt das Datum und die Uhrzeit in der ursprünglichen Nachricht
    val regexForAllPossibleDates = """(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[0-2])\.(\d{4})""".toRegex()
    val regexForAllPossibleTimes = """(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|[1-5][0-9])""".toRegex()
    var messageReplacement = originMessage

    if (regexForAllPossibleDates.containsMatchIn(originMessage).and(regexForAllPossibleTimes.containsMatchIn(originMessage))){
        messageReplacement = originMessage
            .replace(regexForAllPossibleDates, dateReplacement)
            .replace(regexForAllPossibleTimes, timeReplacement)
    }else if (originMessage.contains("dd.MM.yyyy").and(originMessage.contains("HH:mm"))){
        messageReplacement = originMessage
            .replace("dd.MM.yyyy", dateReplacement)
            .replace("HH:mm", timeReplacement)
    }
    return messageReplacement
}


data class ContactZippedWithDate(
    val contactId: Int,
    val date: String,
    val time: String
) // Datenklasse, die Kontakte mit Datum und Uhrzeit verknüpft

data class MutablePairs2(
    var first: Int,
    var second: Boolean
) // Datenklasse für Paare von Kontakt-ID und Status

data class ContactsUiState3(
    val contactUiState: List<Contact> = listOf()
) // Datenklasse zur Verwaltung des UI-Zustands der Kontakte

data class ContactReadyForSms(
    val phoneNumber: String,
    val message: String,
    val fullName: String
) // Datenklasse für Kontakte, die für den SMS-Versand bereit sind

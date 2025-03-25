package com.example.meetingnotification.ui.contact

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.ContactWithEvents
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TAG = ContactCheckBeforeSubmitViewModel::class.simpleName

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

    //alle kalenderereignisse die in der datenbank gespeichert sind, auch abgelaufene oder neue.
    private var _contactWithEvents = MutableStateFlow<List<ContactWithEvents>>(emptyList())
    val contactWithEvents: StateFlow<List<ContactWithEvents>> = _contactWithEvents

    //alle kalendreignisse die wirkich eingetragen sind im kalender aber nicht unbedingt in der db sein müssen.
    private val _calenderState = MutableStateFlow<List<EventDateTitle>>(emptyList())    // MutableStateFlow zur Verwaltung der Kalenderdaten
    private val calenderState: StateFlow<List<EventDateTitle>> = _calenderState         // Unveränderlicher StateFlow zur Abfrage der Kalenderdaten

    //nur die nächsten anstehenden kalenereignisse, pro contact nur ein event möglich.
    private val _calenderStateConnectedToContacts = mutableStateOf<List<ContactZippedWithDate>>(emptyList())            // MutableState zur Verknüpfung von Kontakten mit Kalenderdaten
    val calenderStateConnectedToContacts: State<List<ContactZippedWithDate>> = _calenderStateConnectedToContacts        // Öffentlicher Zugriff auf die verknüpften Kalenderdaten

    private var contactListReadyForSms by mutableStateOf(listOf<ContactReadyForSms>())      // Kontakte, die bereit für den SMS-Versand sind


    fun loadContactsWithEvents(){
        viewModelScope.launch {
            val mutableListContactsWithEvents = mutableListOf<ContactWithEvents>()

            calenderStateConnectedToContacts.value.forEach { contactWithDate ->

                val contactAndEvents = contactRepository.getContactWithEvents(contactWithDate.contactId).first()

                mutableListContactsWithEvents.add(contactAndEvents)
            }
            _contactWithEvents.value = mutableListContactsWithEvents
        }
    }


    fun getContactsReadyForSms(): List<ContactReadyForSms> = contactListReadyForSms         // Gibt die Liste der Kontakte für den SMS-Versand zurück


    fun updateListReadyForSms(contacts: List<ContactReadyForSms>) {    // Aktualisiert die Liste der Kontakte für den SMS-Versand
        contactListReadyForSms = contacts
    }


    fun updateContact(contact: Contact) { // Aktualisiert einen bestimmten Kontakt im Repository. kein supend nötig weil courtinescope unten ausgeführt wird.
        viewModelScope.launch {
            contactRepository.updateItem(contact)
        }
    }

    fun isContactNotifiedForUpcomingEvent(contactId: Int) : Boolean{
        val allEventsForChoosenContact = contactWithEvents.value.firstOrNull { contactWithEvents -> contactWithEvents.contact.id == contactId }?.events ?: emptyList()   //gibt event zurück oder false, falls es keine events hatt.
        if (allEventsForChoosenContact.isEmpty()) return false

        val dateTimeNow = LocalDateTime.now()
        val dateFormated = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val upcomingEventSortedOut = allEventsForChoosenContact.filter { event ->
            LocalDateTime.of(LocalDate.parse(event.eventDate,dateFormated),LocalTime.parse(event.eventTime)).isAfter(dateTimeNow) }.sortedBy { event -> event.eventDate }


        val upcomingEventNotified = upcomingEventSortedOut.firstOrNull()?.isNotified ?: return false

        return upcomingEventNotified
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
                LocalDate.parse(meetingDate, meetingDateFormat)
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
                            date.eventDate.toLocalDate().format(outputFormatterDate),          // Formatiertes Datum
                            date.eventDate.toLocalTime().toString()    // Uhrzeit als Zeichenkette
                        )
                    )
                }
        }

        _calenderStateConnectedToContacts.value = listZipped           // Aktualisiert die MutableState-Liste mit den verknüpften Daten
        deleteEventsThatDontExistsInCalenderAnymoreFromDatabase(dates,contacts)
        insertEventForContact(listZipped)
        updateContactsMessageAfterZippingItWithDates(
            listZipped,
            contacts
        )   // Aktualisiert die Nachrichten der Kontakte nach der Verknüpfung
    }

    private fun deleteEventsThatDontExistsInCalenderAnymoreFromDatabase(allEventsInCalender: List<EventDateTitle>,contactsInDatabase: List<Contact>){
        viewModelScope.launch {
            val outputFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val now = LocalDateTime.now()


            val validEventsInDatabase = contactsInDatabase.flatMap { contact ->
                contactRepository.getContactWithEvents(contact.id).first().events
            }.filter { event ->
                val eventDateTime = LocalDateTime.of(
                    LocalDate.parse(event.eventDate, outputFormatterDate),
                    LocalTime.parse(event.eventTime)
                )
                eventDateTime.isAfter(now)  // Event muss in der Zukunft liegen
            }

            // Events finden, die in der DB sind, aber nicht im Kalender vorkommen
            val eventsToDelete = validEventsInDatabase.filter { validEvent ->
                allEventsInCalender.none { event ->
                    val actualEventDateTime = event.eventDate
                    val validEventDateTime = LocalDateTime.of(
                        LocalDate.parse(validEvent.eventDate, outputFormatterDate),
                        LocalTime.parse(validEvent.eventTime)
                    )
                    actualEventDateTime == validEventDateTime
                }
            }
            Log.i(TAG,"Size of to be deleted Events: ${eventsToDelete.size}")
            //delete Events that are no more in calender but still in Database.
            eventsToDelete.forEach { eventToDelete -> eventRepository.deleteItem(eventToDelete) }
        }
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
    } }

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
    val contactId: Int,
    val phoneNumber: String,
    val message: String,
    val fullName: String
) // Datenklasse für Kontakte, die für den SMS-Versand bereit sind

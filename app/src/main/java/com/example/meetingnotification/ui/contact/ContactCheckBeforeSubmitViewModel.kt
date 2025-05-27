package com.example.meetingnotification.ui.contact

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.repositories.BackgroundImageManagerRepository
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

private val TAG = ContactCheckBeforeSubmitViewModel::class.simpleName

class ContactCheckBeforeSubmitViewModel(
    private val contactRepository: ContactRepository,                // Repository, das zur Datenverwaltung verwendet wird
    private val eventRepository: EventRepository,
    backgroundImageManagerRepository: BackgroundImageManagerRepository
) : ViewModel() {

    init {
        Log.i(TAG,"Viewmode created")
    }

    val selectedBackgroundPictureId: StateFlow<Int> = backgroundImageManagerRepository
        .get().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000), R.drawable.background_picture_1)

    // Ein StateFlow-Objekt, das den aktuellen Zustand der Kontakte verwaltet
    val contactUiState: StateFlow<ContactsUiState3> =
        contactRepository.getAllContactsStream().map { ContactsUiState3(it) }
            .stateIn(
                scope = viewModelScope,                       // Coroutine-Bereich für Nebenläufigkeit
                started = SharingStarted.WhileSubscribed(5_000L), // Teile die Daten, solange abonniert, mit einer Verzögerung von 5000 ms
                initialValue = ContactsUiState3()             // Anfangswert des StateFlow
            )

    //alle kalenderereignisse die in der datenbank gespeichert sind, auch abgelaufene oder neue.
    private val _contactWithEvents = mutableStateOf<List<Event>>(emptyList())
    val contactWithEvents: State<List<Event>> = _contactWithEvents

    //alle kalendreignisse die wirkich eingetragen sind im kalender aber nicht unbedingt in der db sein müssen, keine zurückliegenden vorhanden..
    private val _calenderState = MutableStateFlow<List<EventDateTitle>>(emptyList())    // MutableStateFlow zur Verwaltung der Kalenderdaten
    private val calenderState: StateFlow<List<EventDateTitle>> = _calenderState         // Unveränderlicher StateFlow zur Abfrage der Kalenderdaten

    //nur die nächsten anstehenden kalenereignisse, pro contact nur ein event möglich.
    private val _calenderStateConnectedToContacts = mutableStateOf<List<ContactZippedWithDate>>(emptyList())            // MutableState zur Verknüpfung von Kontakten mit Kalenderdaten
    val calenderStateConnectedToContacts: State<List<ContactZippedWithDate>> = _calenderStateConnectedToContacts        // Öffentlicher Zugriff auf die verknüpften Kalenderdaten

    // Mit by wird der .value wert versteckt aber es ist equivalent zu einem State wie oben, nur das der State mit .value herausgeholt werden muss.
    private var contactListReadyForSms by mutableStateOf(listOf<ContactReadyForSms>())      // Kontakte, die bereit für den SMS-Versand sind

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading


    //region testMethods

//    fun zipDatesToContactsTest(contacts: List<Contact>) {
//            viewModelScope.launch {
//                val duration = measureTimeMillis {
//                    val dates = eventRepository.getAllEventsStream()
//                    val listZipped = mutableListOf<ContactZippedWithDate>()                    // Eine Liste zur Speicherung der verknüpften Daten
//
//                    for (contact in contacts) {
//                        dates.firstOrNull {
//                            it.contactOwnerId == contact.id                // Prüft, ob ein Kalenderereignis zum Kontakt passt
//                        }?.let { date ->                                   // Wenn ein passendes Ereignis gefunden wird
//                                listZipped.add(
//                                    ContactZippedWithDate(
//                                        contact.id,
//                                        date.eventDate,         // Formatiertes Datum
//                                        date.eventTime  // Uhrzeit als Zeichenkette
//                                    )
//                                )
//                            }
//                    }
//                    _calenderStateConnectedToContacts.value = listZipped           // Aktualisiert die MutableState-Liste mit den verknüpften Daten
//                    //deleteEventsThatDontExistsInCalenderAnymoreFromDatabase(dates, contacts)
//                    //insertEventForContact(listZipped)
//                    updateContactsMessageAfterZippingItWithDates(listZipped, contacts)   // Aktualisiert die Nachrichten der Kontakte nach der Verknüpfung
//
//                }.milliseconds
//                Log.d(TAG,"Time to execute zipDatesToContactsTest() $duration")
//            }
//    }
//
//
//    fun loadContactsWithEventsTest() {
//        viewModelScope.launch {
//            val duration = measureTimeMillis {
//                val mutableListContactsWithEvents = mutableListOf<Event>()
//
//                contactUiState.value.contactUiState.forEach { contact ->
//                    val contactAndEvents = eventRepository.getEvents(contact.id)
//                    //Log.d(TAG,"contactWithEventLoaded ${contactAndEvents.contact.firstName}")
//                    mutableListContactsWithEvents.addAll(contactAndEvents)
//                }
//
//                _contactWithEvents.value = mutableListContactsWithEvents
//            }.milliseconds
//            Log.d(TAG,"Time to execute loadContactsWithEventsTest() $duration")
//        }
//    }
//
//    fun isContactNotifiedForUpcomingEventTest(contactId: Int): Boolean {
//        val allEventsForChoosenContact = contactWithEvents.value.filter { contactWithEvents -> contactWithEvents.contactOwnerId == contactId }   //gibt event zurück oder false, falls es keine events hatt.
//        if (allEventsForChoosenContact.isEmpty()) return false
//
//        val dateTimeNow = LocalDateTime.now()
//        val dateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//
//        val upcomingEventSortedOut = allEventsForChoosenContact.filter { event ->
//            LocalDateTime.of(
//                LocalDate.parse(event.eventDate, dateFormated),
//                LocalTime.parse(event.eventTime)
//            ).isAfter(dateTimeNow)
//        }.sortedBy { event -> event.eventDate }
//        val upcomingEventNotified = upcomingEventSortedOut.firstOrNull()?.isNotified ?: return false
//
//        return upcomingEventNotified
//    }

    //endregion


    // Lädt die Kalenderdaten in das MutableStateFlow
    fun loadCalenderData(events: List<EventDateTitle>) {
        _calenderState.value = events
    }


    // Verknüpft Kontakte mit Kalenderdaten
    fun zipDatesToContacts(contacts: List<Contact>) {

        val dates = getCalenderState()                                             // Holt die aktuelle Liste der Kalenderereignisse
        val listZipped = mutableListOf<ContactZippedWithDate>()                    // Eine Liste zur Speicherung der verknüpften Daten
        val outputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Ausgabeformat für das Datum

        val duration = measureTimeMillis {
            for (contact in contacts) {
                dates.firstOrNull {
                    it.eventName.split(" ")[0] == contact.firstName.split(" ")[0] && it.eventName.split(
                        " "
                    )[1] == contact.lastName                       // Prüft, ob ein Kalenderereignis zum Kontakt passt
                }
                    ?.let { date ->                                   // Wenn ein passendes Ereignis gefunden wird
                        listZipped.add(
                            ContactZippedWithDate(
                                contact.id,
                                date.eventDate.toLocalDate()
                                    .format(outputFormatterDate),          // Formatiertes Datum
                                date.eventDate.toLocalTime()
                                    .format(DateTimeFormatter.ofPattern("HH:mm"))    // Uhrzeit als Zeichenkette
                            )
                        )
                    }
            }
        }.milliseconds
        Log.d(TAG, "ZipDatesTocontacts() executionTime = $duration")

        _calenderStateConnectedToContacts.value = listZipped           // Aktualisiert die MutableState-Liste mit den verknüpften Daten
    }


    private fun loadContactsWithEvents() {
        viewModelScope.launch {
            val duration = measureTimeMillis {

                val mutableListContactsWithEvents = mutableListOf<Event>()
                val dateNow = LocalDate.now()
                val contactAndEvents = eventRepository.getEventsAfterToday(dateNow.toString())

                mutableListContactsWithEvents.addAll(contactAndEvents)


                _contactWithEvents.value = mutableListContactsWithEvents
            }.milliseconds
            Log.d(TAG, "LoadContactsWithEvents() executionTime = $duration")
            _isLoading.value = false
            Log.d(TAG, "LoadContactsWithEvents() is executed and loadingScreen is finished.")
        }
    }


    fun isContactNotifiedForUpcomingEvent(contactId: Int): Boolean {
        val upcomingEventNotified: Boolean

        val duration = measureTimeMillis {
            val allEventsForChoosenContact =
                contactWithEvents.value.filter { contactWithEvents -> contactWithEvents.contactOwnerId == contactId }   //gibt event zurück oder false, falls es keine events hatt.
            if (allEventsForChoosenContact.isEmpty()) return false

            //Log.d(TAG, "event for contactId:$contactId = $allEventsForChoosenContact")

            val dateTimeNow = LocalDateTime.now()
            val dateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val upcomingEventSortedOut = allEventsForChoosenContact.filter { event ->
                LocalDateTime.of(
                    LocalDate.parse(event.eventDate, dateFormated),
                    LocalTime.parse(event.eventTime)
                ).isAfter(dateTimeNow)
            }.sortedBy { event -> event.eventDate }


            upcomingEventNotified = upcomingEventSortedOut.firstOrNull()?.isNotified ?: return false
        }.milliseconds
        Log.d(TAG, "isContactNotifiedForUpcomingEvent() executionTime = $duration")
        return upcomingEventNotified
    }


    suspend fun deleteEventsThatDontExistsInCalenderAnymoreFromDatabase(
        allEventsInCalender: List<EventDateTitle> = getCalenderState(),
    ) {
            val duration = measureTimeMillis {
                val outputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val dateNow = LocalDate.now()
                val calenderDateTimes = allEventsInCalender.map { it.eventDate }.toSet()

                val allFutureEvents = eventRepository.getEventsAfterToday(dateNow.toString())

                // Events finden, die in der DB sind, aber nicht im Kalender vorkommen
                val eventsToDelete = allFutureEvents.filter { validEvent ->
                        val eventDateTime = LocalDateTime.of(
                            LocalDate.parse(validEvent.eventDate, outputFormatterDate),
                            LocalTime.parse(validEvent.eventTime)
                        )
                        eventDateTime !in calenderDateTimes

                }
                Log.i(TAG, "Size of to be deleted Events: ${eventsToDelete.size}")
                //delete Events that are no more in calender but still in Database.
                if (eventsToDelete.isNotEmpty()) {
                    eventsToDelete.forEach { eventToDelete ->
                        eventRepository.deleteItem(
                            eventToDelete
                        )
                    }
                    loadContactsWithEvents()
                }
            }.milliseconds
            Log.d(TAG, "deleteEventsThatDontExistsInCalenderAnymoreFromDatabase() executionTime = $duration")
    }


    fun getContactsReadyForSms(): List<ContactReadyForSms> =
        contactListReadyForSms         // Gibt die Liste der Kontakte für den SMS-Versand zurück


    fun updateListReadyForSms(contacts: List<ContactReadyForSms>) {    // Aktualisiert die Liste der Kontakte für den SMS-Versand
        contactListReadyForSms = contacts
    }


    fun updateContact(contact: Contact) { // Aktualisiert einen bestimmten Kontakt im Repository. kein supend nötig weil courtinescope unten ausgeführt wird.
        viewModelScope.launch {
            contactRepository.updateItem(contact)
        }
    }


    suspend fun insertEventForContact(contactZippedWithDate: List<ContactZippedWithDate>) {
            val duration = measureTimeMillis {
                val events = contactZippedWithDate.map {
                    event -> Event(
                    eventDate = event.date,
                    eventTime = event.time,
                    contactOwnerId = event.contactId
                    )
                }

                eventRepository.insertAllEvents(events)
                loadContactsWithEvents()
            }.milliseconds
            Log.d(TAG, "insertEventForContact() executionTime = $duration")
    }


    private fun getCalenderState(): List<EventDateTitle> = calenderState.value      // Gibt die aktuelle Liste der Kalenderereignisse zurück


    // Berechnet die Anzahl der Tage bis zum angegebenen Datum
    fun getDayDuration(meetingDate: String): String {
        val meetingDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Datumsformat für die Berechnung
        val daysBeetweenNowAndMeetingDate = LocalDate.now().until(
            LocalDate.parse(
                meetingDate,
                meetingDateFormat
            )
        ).days // Tage bis zum Datum berechnen
        return "$daysBeetweenNowAndMeetingDate"    // Gibt die verbleibenden Tage als Zeichenkette zurück
    }


    // Aktualisiert die Nachrichten der Kontakte
    fun updateContactsMessageAfterZippingItWithDates(
        zippedDateToContacts: List<ContactZippedWithDate>,
        contactList: List<Contact>
    ) {
        zippedDateToContacts.isNotEmpty()
            .let {                  // Nur wenn verknüpfte Daten vorhanden sind
                viewModelScope.launch {
                    val duration = measureTimeMillis {

                        val contacts = mutableListOf<Contact>()

                        for (zipValue in zippedDateToContacts) { // Durchläuft die Liste der verknüpften Daten
                            val germanDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val dateConvertedToGermanFormat = LocalDate.parse(zipValue.date).format(germanDateFormatter)

                            contactList.firstOrNull { it.id == zipValue.contactId }
                                ?.let { contact ->
                                    contacts.add(Contact(
                                            id = contact.id,
                                            title = contact.title,
                                            firstName = contact.firstName,
                                            lastName = contact.lastName,
                                            sex = contact.sex,
                                            phone = contact.phone,
                                            message = updateMessageWithCorrectDateTime(
                                                contact.message,
                                                dateConvertedToGermanFormat,
                                                zipValue.time
                                            )
                                        )
                                    )
                                }
                        }
                        contactRepository.updateAll(contacts)
                    }.milliseconds
                    Log.d(TAG, "updateContactsMessageAfterZippingItWithDates() executionTime = $duration")
                }
            }
    }
}


// Ersetzt das Datum und die Uhrzeit in der ursprünglichen Nachricht
private fun updateMessageWithCorrectDateTime(
    originMessage: String,
    dateReplacement: String,
    timeReplacement: String
): String {
    val regexForAllPossibleDates =
        """(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[0-2])\.(\d{4})""".toRegex()
    val regexForAllPossibleTimes = """(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|[1-5][0-9])""".toRegex()
    var messageReplacement = originMessage

    if (regexForAllPossibleDates.containsMatchIn(originMessage)
            .and(regexForAllPossibleTimes.containsMatchIn(originMessage))
    ) {
        messageReplacement = originMessage
            .replace(regexForAllPossibleDates, dateReplacement)
            .replace(regexForAllPossibleTimes, timeReplacement)
    } else if (originMessage.contains("dd.MM.yyyy").and(originMessage.contains("HH:mm"))) {
        messageReplacement = originMessage
            .replace("dd.MM.yyyy", dateReplacement)
            .replace("HH:mm", timeReplacement)
    }
    return messageReplacement
}


// Datenklasse, die Kontakte mit Datum und Uhrzeit verknüpft
data class ContactZippedWithDate(val contactId: Int, val date: String, val time: String)

// Datenklasse für Paare von Kontakt-ID und Status
@Parcelize
data class MutablePairs2(var first: Int, var second: Boolean) : Parcelable

// Datenklasse zur Verwaltung des UI-Zustands der Kontakte
data class ContactsUiState3(val contactUiState: List<Contact> = listOf())

// Datenklasse für Kontakte, die für den SMS-Versand bereit sind
data class ContactReadyForSms(
    val contactId: Int,
    val phoneNumber: String,
    val message: String,
    val fullName: String
)

package com.example.meetingnotification.ui.contact

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import com.example.meetingnotification.ui.services.ServiceAction
import com.example.meetingnotification.ui.services.SmsSendingServiceInteractor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val TAG = ContactsSearchScreenViewModel::class.simpleName

class ContactsSearchScreenViewModel(                          // ViewModel zur Verwaltung von Kontakten im Suchbildschirm
    private val contactRepository: ContactRepository,         // Repository für den Zugriff auf die Kontakt-Datenbank
    private val eventRepository: EventRepository              // Repo für Zugriff auf die Event-Datenbank, kein stateflow nötig, weil kein nutzen vorhanden (weil über contact alle events geholt werden), bei contact jedoch doch.
) : ViewModel() {

    val contactsUiState: StateFlow<ContactsUiState2> =
        // StateFlow zur Überwachung des UI-Zustands der Kontakte. Für events ist kein zur Überwachung nötig. Ich kann auch so insert,delete und updaten von events.
        contactRepository.getAllContactsStream()
            .map { ContactsUiState2(it) } // Wandelt die Daten in das UI-Format um
            .stateIn(
                scope = viewModelScope,                       // Coroutine-Bereich des ViewModels für Nebenläufigkeit
                started = SharingStarted.WhileSubscribed(5_000L), // Teilt die Daten weiter, solange abonniert
                initialValue = ContactsUiState2()             // Anfangszustand der Kontakte ist eine leere Liste
            )



    private val contactsWriteOnly = MutableLiveData<List<Contact>>() // MutableLiveData zum Schreiben von Kontakten
    private val contactsReadOnly: LiveData<List<Contact>> get() = contactsWriteOnly // Nur lesbarer Zugriff auf Kontakte von zeile 38.

    private var calenderEvents = listOf<EventDateTitle>()    // Liste der Kalenderereignisse

    var smsServiceInteractor: SmsSendingServiceInteractor? = null // Interaktor für den SMS-Versanddienst


    fun insertContactsToSmsQueue(contacts: List<ContactReadyForSms>) { // Fügt Kontakte zur SMS-Warteschlange hinzu
        smsServiceInteractor?.performServiceAction(ServiceAction.PushContact, contacts)
    }

    fun sendCommandToSendAllMessages(){
        smsServiceInteractor?.performServiceAction(ServiceAction.SendMessage, listOf())
    }


    fun addContactsToDatabase(contactList: List<Contact>, compareIds: List<Int>) // Fügt ausgewählte Kontakte zur Datenbank hinzu. Keine suspend dunction nötig, weil viewmodel scope unten ansynchron ausführt.
    {
        viewModelScope.launch {
            for (id in compareIds) {
                contactList.firstOrNull { contact -> contact.id == id }?.let { matchingContact ->   // Speichert den passenden Kontakt im Repository pfalls richtiger contact mit passender id gefunden wird.
                    contactRepository.insertItem(matchingContact)
                }
            }
        }
    }

    fun updateContactInDatabase(contact: Contact){
        viewModelScope.launch {
            contactRepository.updateItem(contact)
        }
    }

    @SuppressLint("Range", "CheckResult")
    fun loadContacts(context: Context) {                     // Lädt die Kontakte aus dem System-Kontaktbuch
        val contactList = mutableListOf<Contact>()           // Liste für die geladenen Kontakte
        val contentResolver = context.contentResolver        // Holt den Content Resolver für Datenbank-Abfragen
        var isContactJustToUpdate = false

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME.split(" ")[0] + " ASC"     // Sortierung nach Vorname
        )                                                                                       // Abfrage aller Kontakte aus der Standard-Kontaktdatenbank des Systems

        if (cursor != null && cursor.count > 0) {            // Wenn es Ergebnisse gibt
            while (cursor.moveToNext()) { // Durchläuft jeden Kontakt im Cursor

                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)) // Kontakt-ID

                val fullname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).split(" ")           // Aufteilen des vollen Namens in Vor- und Nachname

                val firstname = fullname[0]                               // Vorname

                val surname = fullname[if (fullname.size > 2) 2 else 1]   // Nachname, abhängig von der Namensstruktur

                val sex = if (fullname.size > 2) fullname[1] else "X"     // Geschlecht (männlich/weiblich/unspezifisch)

                val orgCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                    null
                )// Abfrage der Organisation des Kontakts

                var title = "none"                            // Standardwert für den Titel
                if (orgCursor != null && orgCursor.moveToFirst()) {
                    title = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)) // Titel der Organisation
                }
                orgCursor?.close()

                // Abfrage der Telefonnummer(n)
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (phoneCursor?.moveToNext() == true) {
                        val phoneNumber = phoneCursor.getString(
                            phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        val isMale = sex.lowercase() == "m"               // Prüft, ob das Geschlecht männlich ist
                        val defaultMessage = context.resources.getString( // Standardnachricht für jeden Kontakt mit Platzhaltern
                                R.string.default_message,
                                if (isMale) "r" else "",
                                if (isMale) "Herr" else "Frau",
                                if (title != "none") "$title " else "",
                                surname,
                                "dd.MM.yyyy",
                                "HH:mm"
                            )

                        contactList.add(Contact
                            (                                      // Fügt den Kontakt zur Liste hinzu
                                id.toInt(),
                                title,
                                firstname,
                                surname,
                                sex[0].uppercaseChar(),
                                phoneNumber,
                                defaultMessage
                            )
                        )

                        val contactFromDatabaseIfExists = contactsUiState.value.contactList.firstOrNull { contact -> contact.id == id.toInt()}

                        contactFromDatabaseIfExists?.let {
                            if (contactFromDatabaseIfExists.lastName != surname ||
                                contactFromDatabaseIfExists.firstName != firstname ||
                                contactFromDatabaseIfExists.sex.toString() != sex ||
                                contactFromDatabaseIfExists.title != title ||
                                contactFromDatabaseIfExists.phone != phoneNumber){
                                Log.d(TAG,"Contact is just to update in database()")
                                isContactJustToUpdate = true
                            }
                            if (isContactJustToUpdate){
                                updateContactInDatabase(contactFromDatabaseIfExists.copy(firstName = firstname, lastName = surname, title = title, phone = phoneNumber, sex = sex[0], message = defaultMessage))
                            }
                        }

                    }
                    phoneCursor?.close()
                }
            }
            cursor.close()                                                //Schließt den cursor wenn fertig, wichtig!
        }

        // Aktualisiert die Kontakte in der Live-Datenstruktur. Wichtig : postValue() überschreibt immer alle alten Werte.
        //Wenn wir neue Werte anhängen wollen, bleibt keine andere Möglichkeit, als die alte liste irgendwo abzuspeichern und dann neue einfügen und am schluss wiieder mit postValue() updaten.
        contactsWriteOnly.postValue(contactList)
    }

    fun getContacts(): LiveData<List<Contact>> {                          // Gibt die Live-Datenstruktur für Kontakte zurück
        return contactsReadOnly
    }


    @SuppressLint("Range")
    fun loadCalender(context: Context) { // Lädt Kalenderereignisse aus der Systemdatenbank
        val eventList = mutableListOf<EventDateTitle>()       // Liste der geladenen Ereignisse
        val contentResolver = context.contentResolver // Holt den Content Resolver für Datenbank-Abfragen

        val todayMillis = System.currentTimeMillis()

        // Führt eine Abfrage auf die Kalender-Datenbank durch, um alle zukünftigen Ereignisse ab dem heutigen Tag zu erhalten, sortiert nach Startzeit
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,                         // URI, die auf die Kalenderereignisse verweist
            null,                                              // Spalten, die zurückgegeben werden sollen (null bedeutet alle Spalten)
            "${CalendarContract.Events.DTSTART} >= ?",         // WHERE-Klausel, um Ereignisse ab dem heutigen Tag zu filtern. Das ? ist der platzhalter.
            arrayOf(todayMillis.toString()),                           // Argumente für die Platzhalter in der WHERE-Klausel (heutiges Datum in Millisekunden)
            CalendarContract.Events.DTSTART + " ASC"          // ORDER BY-Klausel, um die Ereignisse nach Startzeit aufsteigend zu sortieren
        )                                                             //Die ganze abfrage oben ist äquvivalent zu dieser SQL abfrage : SELECT * FROM events WHERE DTSTART >= 1704115200000 ORDER BY DTSTART ASC;

        if (cursor != null && cursor.count > 0) {             // Wenn Ereignisse gefunden werden
            while (cursor.moveToNext()) { // Durchläuft die Ereignisse im Cursor

                val title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)) // Titel des Ereignisses

                val startTimeMilis = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))         // Startzeit in Millisekunden

                val startEvent = Instant.ofEpochMilli(startTimeMilis)                              // Konvertiert in ein LocalDateTime-Objekt
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                eventList.add(EventDateTitle
                    (
                        startEvent,                            // Startzeit als LocalDateTime
                        title                                  // Titel des Ereignisses
                    )
                )
            }
            cursor.close()                                    //cursor wider schließen wenn fertig, wiichtig!
        }
        calenderEvents = eventList                            // Aktualisiert die Kalenderereignisliste
    }

    fun getCalender(): List<EventDateTitle> {                 // Gibt die aktuelle Liste der Kalenderereignisse zurück
        return calenderEvents
    }
}

data class MutablePairs(var first: Int, var second: Boolean)  // Datenklasse für Paare (ID, Status)

data class ContactsUiState2(val contactList: List<Contact> = listOf()) // Datenklasse für den Kontaktzustand der UI

data class EventDateTitle(val eventDate: LocalDateTime, val eventName: String) // Datenklasse für Ereignisse (Datum, Name)

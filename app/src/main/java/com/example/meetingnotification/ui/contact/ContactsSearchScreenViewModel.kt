package com.example.meetingnotification.ui.contact

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.net.Uri
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
import java.util.TimeZone

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
        smsServiceInteractor?.performServiceActionToAddOrSend(ServiceAction.PushContact, contacts)
    }

    fun getContactsFromSmsQueue(): List<Int>? {
        return smsServiceInteractor?.performServiceActionToGetContactFromQueue(ServiceAction.GetContactsFromQueue)
    }

    fun sendCommandToSendAllMessages(){
        smsServiceInteractor?.performServiceActionToAddOrSend(ServiceAction.SendMessage, listOf())
    }

    fun removeContactIfInSmsQueue(contactId : Int){
        smsServiceInteractor?.performServiceActionToRemoveFromQueue(ServiceAction.DeleteContactFromQueue,contactId)
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

    private fun updateContactInDatabase(contact: Contact){
        viewModelScope.launch {
            contactRepository.updateItem(contact)
        }
    }

    //Nötig um loadContacts asynchron laufen zu lassen, um darrin suspende funktionen auf die datenbank aufzurufen um einen contact zu erhalten.
    fun loadContactsWrapper(context: Context){
        viewModelScope.launch {
            loadContacts(context)
        }
    }

    @SuppressLint("Range", "CheckResult")
    suspend fun loadContacts(context: Context) {                     // Lädt die Kontakte aus dem System-Kontaktbuch
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
                        //Hier wird geprüft ob contact schon in der datenbank eingetragen ist.
                        val contactFromDatabaseIfExists = contactRepository.getContactStream(id.toInt())

                        //Wenn sich name von kontakt ändert im Telefon dann wird dieser in der datenbank room geupdated.
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
                                updateContactInDatabase(Contact(id.toInt(),title,firstname,surname,sex[0],phoneNumber,defaultMessage))
                                isContactJustToUpdate = false
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

    //region Test Insert Contacts/Events in Phone. Remove in Release

    fun insertContacts(context: Context) {

        fun loadAllValidRecords(): List<ContactSimpleTest> {
            val contacts = mutableListOf<ContactSimpleTest>()

            try {
                context.assets.open("insertContactsTestData.txt").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(";")
                        if (parts.size == 4) {
                            val contact = ContactSimpleTest(
                                firstName = parts[0],
                                surname = parts[1],
                                title = parts[2],
                                number = parts[3]
                            )
                            contacts.add(contact)
                            Log.d(TAG, "Kontakt geladen: $contact")
                        } else {
                            Log.w(TAG, "Ungültiges Format in Zeile: $line")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Laden der Kontakte aus Assets", e)
            }
            return contacts
        }

        val listOfDummyContactsExtracted = loadAllValidRecords()

        listOfDummyContactsExtracted.forEach { dummyContact ->

            val ops = ArrayList<ContentProviderOperation>()

            // 1. Leeren RawContact erzeugen
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // 2. Titel (prefix)
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, dummyContact.title)
                    .build()
            )

            // 3. Vor- & Nachname
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        dummyContact.firstName
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        dummyContact.surname
                    )
                    .build()
            )

            // 4. Telefonnummer
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, dummyContact.number)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .build()
            )

            // Ausführen
            try {
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                Log.d(TAG,"Contact $dummyContact hinzugefügt!")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG,"Kontakt $dummyContact hinzufügen fehlgeschlagen!")
            }

        }
    }


    fun insertEvents(context: Context) {
        fun getCalendarId(context: Context): Long? {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
            )
            val uri = CalendarContract.Calendars.CONTENT_URI
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val name = it.getString(1)
                    Log.d(TAG, "ID: $id, Name: $name")
                    if (name.contains("Google") || name.contains("Kalender") || name.contains("simba.ibrahimovic6@gmail.com")) {
                        return id
                    }
                }
            }
            return null
        }

        fun loadAllValidRecords(): List<EventSimpleTest> {
            val events = mutableListOf<EventSimpleTest>()
            try {
                context.assets.open("insertEventsTestData.txt").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(";")
                        if (parts.size == 3) {
                            val event = EventSimpleTest(
                                title = parts[0],
                                startTime = LocalDateTime.parse(parts[1]).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() ,
                                endTime = LocalDateTime.parse(parts[2]).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            )
                            events.add(event)
                            Log.d(TAG, "Events geladen: $event")
                        } else {
                            Log.w(TAG, "Ungültiges Format in Zeile: $line")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Laden der Evente aus Assets", e)
            }
            return events
        }

        val listOfDummyEventsExtracted = loadAllValidRecords()

        listOfDummyEventsExtracted.forEach { event ->
            val calendarId = getCalendarId(context)

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, event.startTime)
                put(CalendarContract.Events.DTEND, event.endTime)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }
    }

    //endregion


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

data class ContactSimpleTest(val firstName: String, val surname: String, val title: String, val number: String) //Test, remove on release.

data class EventSimpleTest(val startTime: Long,val endTime: Long,val title: String)

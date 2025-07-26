package com.example.meetingnotification.ui.broadcastReceiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import com.example.meetingnotification.ui.contact.EventDateTitle
import com.example.meetingnotification.ui.data.ContactDatabase
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.utils.DebugUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar


private val TAG = WeeklyEventDbUpdater::class.simpleName
class WeeklyEventDbUpdater: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"WeeklyEventDbUpdater called()")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "SET_ALARM_FOR_EVENT_DB_UPDATER") {

            CoroutineScope(Dispatchers.IO).launch {
                DebugUtils.logExecutionTime(TAG,"Performance measure()"){

                    val eventsInCalender = loadCalender(context)
                    if (eventsInCalender.isEmpty()) return@launch
                    Log.d(TAG, "eventsInCalender loaded()")
                    Log.d(TAG, "eventsInCalender data $eventsInCalender")

                    val contacsInDatabase = loadContactsFromDatabase(context)
                    if (contacsInDatabase.isEmpty()) return@launch
                    Log.d(TAG, "contactsInDatabase loaded()")
                    Log.d(TAG, "contactsInDatabase data: $contacsInDatabase")

                    val contactsMap = contacsInDatabase.associateBy { it.firstName to it.lastName }

                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                    val matchedContacts = eventsInCalender.mapNotNull { event ->
                        val parts = event.eventName.split(" ")
                        if (parts.size < 2) return@mapNotNull null
                        val key = parts[0] to parts[1]
                        val contact = contactsMap[key]
                        if (contact != null) {
                            ContactEvent(
                                id = contact.id,
                                firstName = contact.firstName,
                                lastName = contact.lastName,
                                eventDate = event.eventDate.format(dateFormatter),
                                eventTime = event.eventDate.format(timeFormatter)
                            )
                        } else null
                    }.toSet()
                    Log.d(TAG,"matchedContacts : $matchedContacts")

                    insertEventsOffline(context,matchedContacts)
                    Log.d(TAG,"Events inserted()")


                    Log.d(TAG,"New alarm register started()")

                    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, WeeklyEventDbUpdater::class.java)
                    intent.action = "SET_ALARM_FOR_EVENT_DB_UPDATER"

                    val nextIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) // Oder beliebigen Tag
                        set(Calendar.HOUR_OF_DAY, 18) // Deine gewünschte Uhrzeit
                        set(Calendar.MINUTE, 30)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) add(Calendar.DATE, 7)
                    }

                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        nextIntent
                    )
                    Log.d(TAG,"New alarm registered()")
                }
            }

        }
        Log.d(TAG,"WeeklyEventDbUpdater finished()")
    }

    private suspend fun loadContactsFromDatabase(context: Context): List<Contact>{
        try {
            val dao = ContactDatabase.getDatabase(context).contactDao().getAllContacts().first()
            return dao
        }catch (e: NoSuchElementException){
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e(TAG,"Geting contacts from db failed, bcs no contact was inserted yet. Empty list will be returned.")
        }
        return emptyList()
    }

    private suspend fun insertEventsOffline(context: Context, eventsRaw: Set<ContactEvent>){
        val dao = ContactDatabase.getDatabase(context).eventDAO()
        val eventsConverted = eventsRaw.map { event -> Event(eventDate = event.eventDate, eventTime = event.eventTime, contactOwnerId = event.id) }
        dao.insertAll(eventsConverted)
    }

    @SuppressLint("Range")
    fun loadCalender(context: Context): List<EventDateTitle> { // Lädt Kalenderereignisse aus der Systemdatenbank

        val eventList = mutableListOf<EventDateTitle>()       // Liste der geladenen Ereignisse
        val contentResolver = context.contentResolver // Holt den Content Resolver für Datenbank-Abfragen

        val now = LocalDateTime.now()
        val of = ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())

        val todayMillis = now.toInstant(of).toEpochMilli()
        val todayPlusSevenDaysMillis = LocalDateTime.now().plusDays(8).toInstant(of).toEpochMilli()


        // Führt eine Abfrage auf die Kalender-Datenbank durch, um alle zukünftigen Ereignisse ab dem heutigen Tag zu erhalten, sortiert nach Startzeit
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,                         // URI, die auf die Kalenderereignisse verweist
            null,                                              // Spalten, die zurückgegeben werden sollen (null bedeutet alle Spalten)
            "${CalendarContract.Events.DTSTART} BETWEEN ? AND ? AND ${CalendarContract.Events.DELETED} != 1",         // WHERE-Klausel, um Ereignisse ab dem heutigen Tag zu filtern. Das ? ist der platzhalter.
            arrayOf(
                todayMillis.toString(),
                todayPlusSevenDaysMillis.toString()
            ),                           // Argumente für die Platzhalter in der WHERE-Klausel (heutiges Datum in Millisekunden)
            CalendarContract.Events.DTSTART + " ASC"          // ORDER BY-Klausel, um die Ereignisse nach Startzeit aufsteigend zu sortieren
        )                                                             //Die ganze abfrage oben ist äquvivalent zu dieser SQL abfrage : SELECT * FROM events WHERE DTSTART >= 1704115200000 ORDER BY DTSTART ASC;

        if (cursor != null && cursor.count > 0) {             // Wenn Ereignisse gefunden werden
            while (cursor.moveToNext()) { // Durchläuft die Ereignisse im Cursor

                var title: String

                if (cursor.getColumnIndex(CalendarContract.Events.TITLE) != -1) {
                    try {
                        title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)) // Titel des Ereignisses
                    }catch (e: NullPointerException){
                        e.printStackTrace()
                        continue
                    }
                }else {
                    continue
                }

                var startTimeMilis:Long

                if (cursor.getColumnIndex(CalendarContract.Events.DTSTART) != -1) {
                    try {
                        startTimeMilis = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))         // Startzeit in Millisekunden
                    }catch (e: NullPointerException){
                        e.printStackTrace()
                        continue
                    }
                }else{
                    continue
                }

                val startEvent =
                    Instant.ofEpochMilli(startTimeMilis)                              // Konvertiert in ein LocalDateTime-Objekt
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                eventList.add(
                    EventDateTitle
                        (startEvent,                            // Startzeit als LocalDateTime
                        title                                  // Titel des Ereignisses
                    )
                )
            }
            cursor.close()                                    //cursor wider schließen wenn fertig, wiichtig!
        }
        return eventList                            // Aktualisiert die Kalenderereignisliste
    }

}

data class ContactEvent(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val eventDate: String,   // Format: yyyy-MM-dd
    val eventTime: String    // Format: HH:mm
)
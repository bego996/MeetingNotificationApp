package com.example.meetingnotification.ui.broadcastReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import com.example.meetingnotification.ui.contact.EventDateTitle
import com.example.meetingnotification.ui.data.ContactDatabase
import com.example.meetingnotification.ui.data.entities.Contact
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


private val TAG = WeeklyEventDbUpdater::class.simpleName
class WeeklyEventDbUpdater: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"WeeklyEventDbUpdater called()")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "SET_ALARM_FOR_EVENT_DB_UPDATER") {

            CoroutineScope(Dispatchers.IO).launch {
                val eventsInCalender = loadCalender(context).toSet()
                if (eventsInCalender.isEmpty()) return@launch
                Log.d(TAG, "eventsInCalender loaded()")
                Log.d(TAG, "eventsInCalender data $eventsInCalender")
                val contacsInDatabase = loadContactsFromDatabase(context).toSet()
                if (contacsInDatabase.isEmpty()) return@launch
                Log.d(TAG, "contactsInDatabase loaded()")
                Log.d(TAG, "contactsInDatabase data: $contacsInDatabase")
            }

        }
        Log.d(TAG,"WeeklyEventDbUpdater finished()")
    }

    suspend fun loadContactsFromDatabase(context: Context): List<Contact>{
        try {
            val dao = ContactDatabase.getDatabase(context).contactDao().getAllContacts().first()
            return dao
        }catch (e: NoSuchElementException){
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return emptyList()
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
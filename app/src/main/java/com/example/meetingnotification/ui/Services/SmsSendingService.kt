package com.example.meetingnotification.ui.services

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import com.example.meetingnotification.ui.broadcastReceiver.SmsSentReceiver
import com.example.meetingnotification.ui.contact.ContactReadyForSms
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SmsSendingService : Service() {                         // Dienst(Service), der SMS-Nachrichten versendet
    private lateinit var receiver: SmsSentReceiver            // Deklariert einen SMS-Empfänger(Broadcast)
    private lateinit var contactRepository: ContactRepository
    private lateinit var eventRepository: EventRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val binder = LocalBinder()                        // Binder für die Client-Anbindung an den Dienst
    var messageQueue = ArrayDeque<SmsMessage>()               // Warteschlange für SMS-Nachrichten

    fun initialize(contactRepository: ContactRepository,eventRepository: EventRepository){
        this.contactRepository = contactRepository
        this.eventRepository = eventRepository
    }

    inner class LocalBinder : Binder() {                                // Innere Klasse, die den Binder für den lokalen Dienst bereitstellt
        fun getService(): SmsSendingService = this@SmsSendingService    // Gibt die aktuelle Instanz des Dienstes zurück
    }

    override fun onBind(intent: Intent): IBinder {            // Methode zum Binden des Dienstes an einen Client
        return binder                                         // Gibt den Binder zurück
    }


    override fun onCreate() {                                 // Wird beim Erstellen des Dienstes aufgerufen
        super.onCreate()
        println("ServiceOnCreate()")
        receiver = SmsSentReceiver(this)                      // Initialisiert den SMS-Empfänger
        val intentFilter = IntentFilter("SMS_SENT")            // Filtert nach dem Broadcast "SMS_SENT"
        registerReceiver(receiver, intentFilter)              // Registriert den Empfänger für das entsprechende Intent
        println("Receiver is registered")
    }


    override fun onDestroy() {                                // Wird beim Zerstören des Dienstes aufgerufen
        unregisterReceiver(receiver)                          // Hebt die Registrierung des Empfängers auf
        super.onDestroy()
        println("Receiver is unregistere an SmS service Destroyed")
    }

    //Callback function. Function to get the upcoming event for specific contact from the database.
    fun getUpcomingEventForContact(contactId: Int,callback: (Event) -> Unit){
        val dateTimeNow = LocalDateTime.now()
        val dateFormated = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        serviceScope.launch {
            try {
                val allEventsForChoosenContact = contactRepository.getContactWithEvents(contactId).first().events

                val upcomingEventSortedOut = allEventsForChoosenContact.filter { event ->
                    LocalDateTime.of(LocalDate.parse(event.eventDate,dateFormated), LocalTime.parse(event.eventTime)).isAfter(dateTimeNow) }.sortedBy { event -> event.eventDate }[0]

                callback(upcomingEventSortedOut)
            }catch (e: NoSuchElementException){
                throw NoSuchElementException("No events found for contactId: $contactId")
            }
        }
    }

    //will be called when a event for a contact is already notified. It updates the Event in the database.
    fun updateEventInDatabase(event: Event){
        serviceScope.launch {
            eventRepository.updateItem(event)
        }
    }

    fun addMessageToQueue(contactInformation: List<ContactReadyForSms>) {                                   // Fügt eine Liste von Kontakten zur SMS-Warteschlange hinzu
        val contactMaper = contactInformation.map { SmsMessage(it.contactId,it.phoneNumber, it.message, it.fullName) }   // Wandelt die Kontakte in SMS-Nachrichten um
        contactMaper.forEach { contact ->
            if (!messageQueue.contains(contact)) {            // Vermeidet das Hinzufügen doppelter Nachrichten
                messageQueue.add(contact)                     // Fügt die Nachricht zur Warteschlange hinzu
            }
        }
    }

    fun getContactsInSmsQueueWithId() : List<Int> = messageQueue.toList().map { contact -> contact.contactId}


    fun sendNextMessage(context: Context) {                   // Sendet die nächste Nachricht aus der Warteschlange
        println("sendNextMessage is called()")
        if (messageQueue.isNotEmpty()) {                      // Prüft, ob die Warteschlange leer ist
            val nextMessage = messageQueue.removeFirst()      // Holt die erste Nachricht und entfernt sie aus der Warteschlange
            val uniqueRequestCode = nextMessage.contactId       //diesen unique code brauce ich weil ab api 31 nicht mehr FlAG_IMUTABLE (mit dem konnte man vor api 31 die extras aktualisieren im gleichen intent) verwendet werden kann.

            val smsIntent = PendingIntent.getBroadcast(
                context, uniqueRequestCode, Intent("SMS_SENT").apply {  //hier kommt der unique requestcode rein. In meinem fall die contact id sowie unten (aber mit echtem namen contactId).
                    putExtra("contactId",nextMessage.contactId)     //Fügt die contactId zum Intent hinzu um den Receiver die id des erfolgreich gesendeten nachricht an contacts zu geben.
                },     // Erzeugt ein PendingIntent für das "SMS_SENT"-Broadcast
                PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {    // Für neuere Android-Versionen
                getSystemService(SmsManager::class.java).sendTextMessage(
                    nextMessage.phoneNumber,                  // Telefonnummer des Empfängers
                    null,
                    nextMessage.message,                      // Nachrichtentext
                    smsIntent, // PendingIntent für das Ergebnis
                    null
                )
            } else {
                SmsManager.getDefault().sendTextMessage(
                    nextMessage.phoneNumber,
                    null,
                    nextMessage.message,
                    smsIntent,
                    null
                )
            }
        }
    }


    fun showMessageSendDialog(context: Context, fullName: String, onResult: (Boolean) -> Unit) {            // Zeigt einen Bestätigungsdialog an
        val builder = AlertDialog.Builder(context)           // Erstellt einen Dialog-Builder
        builder.setTitle("SmS Send Request")                  // Setzt den Titel des Dialogs
        builder.setMessage("Do you want to send the notification Message to this contacts: \n$fullName ?")  // Fragt den Benutzer, ob die Nachricht gesendet werden soll

        builder.setPositiveButton("Accept") { dialog, which ->                  // Akzeptiert das Senden der Nachricht
            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()      // Zeigt eine Toast-Nachricht an
            onResult(true)                                                           // Setzt das Ergebnis auf `true`
        }

        builder.setNegativeButton("Deny") { dialog, which ->                    // Lehnt das Senden der Nachricht ab
            Toast.makeText(context, "Denied", Toast.LENGTH_SHORT).show()        // Zeigt eine Toast-Nachricht an
            onResult(false)                                                          // Setzt das Ergebnis auf `false`
        }

        builder.create().show()                                                      // Erstellt und zeigt den Dialog an
    }

    data class SmsMessage(val contactId: Int,val phoneNumber: String, val message: String, val fullName: String)   // Datenklasse für eine SMS-Nachricht
}



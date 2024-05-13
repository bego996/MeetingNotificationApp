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

class SmsSendingService : Service() {                         // Dienst(Service), der SMS-Nachrichten versendet
    private lateinit var receiver: SmsSentReceiver            // Deklariert einen SMS-Empfänger(Broadcast)
    private val binder = LocalBinder()                        // Binder für die Client-Anbindung an den Dienst
    var messageQueue = ArrayDeque<SmsMessage>()               // Warteschlange für SMS-Nachrichten

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
    }


    fun addMessageToQueue(contactInformation: List<ContactReadyForSms>) {                                   // Fügt eine Liste von Kontakten zur SMS-Warteschlange hinzu
        val contactMaper = contactInformation.map { SmsMessage(it.phoneNumber, it.message, it.fullName) }   // Wandelt die Kontakte in SMS-Nachrichten um
        contactMaper.forEach { contact ->
            if (!messageQueue.contains(contact)) {            // Vermeidet das Hinzufügen doppelter Nachrichten
                messageQueue.add(contact)                     // Fügt die Nachricht zur Warteschlange hinzu
            }
        }
    }


    fun sendNextMessage(context: Context) {                   // Sendet die nächste Nachricht aus der Warteschlange
        println("sendNextMessage is called()")
        if (messageQueue.isNotEmpty()) {                      // Prüft, ob die Warteschlange leer ist
            val nextMessage = messageQueue.removeFirst()      // Holt die erste Nachricht und entfernt sie aus der Warteschlange

            val smsIntent = PendingIntent.getBroadcast(
                context, 0, Intent("SMS_SENT"),     // Erzeugt ein PendingIntent für das "SMS_SENT"-Broadcast
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

    data class SmsMessage(val phoneNumber: String, val message: String, val fullName: String)   // Datenklasse für eine SMS-Nachricht
}



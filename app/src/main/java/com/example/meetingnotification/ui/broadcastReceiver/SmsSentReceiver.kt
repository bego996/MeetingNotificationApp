package com.example.meetingnotification.ui.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.meetingnotification.ui.data.entities.DateMessageSent
import com.example.meetingnotification.ui.services.SmsSendingService
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TAG = SmsSentReceiver::class.simpleName     // Deklariert eine private Konstante `TAG` mit dem Namen der Klasse `SmsSentReceiver` zur Verwendung in Log-Nachrichten

class SmsSentReceiver(private val service: SmsSendingService) : BroadcastReceiver() {   // Eine Klasse, die `BroadcastReceiver` erweitert und eine `SmsSendingService`-Instanz empfängt

    override fun onReceive(context: Context, intent: Intent) { // Überschreibt die `onReceive`-Methode, um auf Broadcasts zu reagieren
        Log.d(TAG, "smsReceiverCalled() with ResultCode: $resultCode")      // Loggt eine Debug-Nachricht, dass der Receiver aktiviert wurde
        if (resultCode == Activity.RESULT_OK || resultCode == 4 || resultCode == 1) {     // Überprüft, ob die SMS erfolgreich gesendet wurde, indem das `resultCode` mit `RESULT_OK` verglichen wird. 4 war für ein anderes Device als OK.

            val contactId = intent.getIntExtra("contactId",-1)  //Hier wird der Integer Extra entnommen der im intent übergeben wurde.
            val messageQueueSize = intent.getIntExtra("SmsQueueSize",-1)
            val actualDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val actualTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            Log.d(TAG,"Message for contactId: $contactId is send.")
            Log.d(TAG,"Size of the Message Queue: $messageQueueSize.")

            //When last message is send in Queue, then update the lastTimeDate Composable text in HomeScreen.kt
            if (messageQueueSize == 0){
                service.insertDatesForSendMessages(DateMessageSent(lastTimeSendet = actualTime, lastDateSendet = actualDate))
            }

            //Here needs to await the callback value before next code is proceding.
            service.getUpcomingEventForContact(contactId) { event ->
                event.let {
                    Log.d(TAG, "Event with contactOwnerId: ${it.contactOwnerId}")

                    // Event aktualisieren und in der Datenbank speichern
                    val updatedEvent = it.copy(isNotified = true)
                    service.updateEventInDatabase(updatedEvent)
                    Log.d(TAG, "Upcoming Event set to notified = true")

                    service.sendNextMessage(context)
                    Log.d(TAG, "NextMessageSend()")
                }
            }
        }
    }
}
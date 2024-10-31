package com.example.meetingnotification.ui.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.meetingnotification.ui.services.SmsSendingService

private val TAG = SmsSentReceiver::class.simpleName     // Deklariert eine private Konstante `TAG` mit dem Namen der Klasse `SmsSentReceiver` zur Verwendung in Log-Nachrichten

class SmsSentReceiver(private val service: SmsSendingService) : BroadcastReceiver() {   // Eine Klasse, die `BroadcastReceiver` erweitert und eine `SmsSendingService`-Instanz empfängt

    override fun onReceive(context: Context, intent: Intent) { // Überschreibt die `onReceive`-Methode, um auf Broadcasts zu reagieren
        Log.d(TAG, "smsReceiverCalled() with ResultCode: $resultCode")      // Loggt eine Debug-Nachricht, dass der Receiver aktiviert wurde
        if (resultCode == Activity.RESULT_OK || resultCode == 4) {     // Überprüft, ob die SMS erfolgreich gesendet wurde, indem das `resultCode` mit `RESULT_OK` verglichen wird. 4 war für ein anderes Device als OK.
            val contactId = intent.getIntExtra("contactId",-1)  //Hier wird der Integer Extra entnommen der im intent übergeben wurde.
            Log.d(TAG,"Message for contactId: $contactId is send.")
            service.updateEventStatusFromContact(contactId)
            service.sendNextMessage(context)        // Wenn das Ergebnis erfolgreich ist, ruft die `sendNextMessage`-Methode auf dem `service`-Objekt auf
        }
    }
}
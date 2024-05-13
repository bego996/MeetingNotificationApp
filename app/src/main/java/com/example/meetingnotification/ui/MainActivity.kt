package com.example.meetingnotification.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.meetingnotification.ui.contact.ContactReadyForSms
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.services.ServiceAction
import com.example.meetingnotification.ui.services.SmsSendingService
import com.example.meetingnotification.ui.services.SmsSendingServiceInteractor

class MainActivity : AppCompatActivity(), SmsSendingServiceInteractor {     // Hauptaktivität der App, die den SMS-Sending-Service Interactor implementiert

    companion object {                                                      // Begleitendes statisches Objekt für Konfigurationskonstanten
        private const val REQUEST_CODE_CONTACTS_READ = 1                    // Anfragecode für Lesezugriff auf Kontakte
        private const val REQUEST_CODE_KALENDER_READ = 2                    // Anfragecode für Lesezugriff auf Kalender
        private const val REQUEST_CODE_SEND_SMS = 3                         // Anfragecode für Senden von SMS
    }

    private val contactBuffer by viewModels<ContactsSearchScreenViewModel> { AppViewModelProvider.Factory }     // ViewModel für die Kontaktsuche

    private lateinit var smsService: SmsSendingService                 // Später zu initialisierender SMS-Dienst
    private var isSmsServiceBound = false                              // Status, ob der SMS-Dienst gebunden ist

    private val smsServiceConnection = object : ServiceConnection {    // Objekt für die Verbindung zum SMS-Dienst
        override fun onServiceConnected(className: ComponentName, service: IBinder) { // Wird aufgerufen, wenn die Verbindung hergestellt ist
            val binder = service as SmsSendingService.LocalBinder      // Holt den Binder vom SMS-Dienst
            smsService = binder.getService()                           // Holt die Dienstinstanz
            isSmsServiceBound = true                                   // Setzt den Verbindungsstatus auf "gebunden"
            println("serviceConnected()")                              // Debug-Nachricht zur Bestätigung
        }
        override fun onServiceDisconnected(arg0: ComponentName) {      // Wird aufgerufen, wenn die Verbindung getrennt wird
            isSmsServiceBound = false                                  // Setzt den Verbindungsstatus auf "nicht gebunden"
            println("serviceDisconected()")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {               // Wird beim Erstellen der Aktivität aufgerufen
        super.onCreate(savedInstanceState)
        println("onCreate() - MainActivity")

        checkAndRequestPermissions()                                   // Überprüft und fordert erforderliche Berechtigungen an
        contactBuffer.smsServiceInteractor = this                      // Verknüpft das ViewModel mit dem Dienst-Interface. Wichtige schnittstelle , weil im viewmodel kein service erstellt werden darf.

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Green
            ) {
                NotificationApp(
                    viewModel = contactBuffer,
                    sendMessage = {                                    // Callback für das Senden von Nachrichten
                        isSmsServiceBound.takeIf { it }?.let {
                            smsService.showMessageSendDialog(           // Zeigt ein Dialogfenster für die Nachricht an
                                this@MainActivity,
                                smsService.messageQueue.takeIf { it.isNotEmpty() }?.map { it.fullName }.toString() // Holt den vollständigen Namen aus der Warteschlange
                            ) { accepted ->                             // Ergebnis-Callback des Dialogs
                                if (accepted) {
                                    smsService.sendNextMessage(this@MainActivity) // Sendet die nächste Nachricht, wenn akzeptiert
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onStart() {                                           // Wird beim Start der Aktivität aufgerufen
        super.onStart()

        Intent(this, SmsSendingService::class.java).also { intent ->   // Erstellt einen Intent für den SMS-Dienst
            bindService(intent, smsServiceConnection, BIND_AUTO_CREATE)             // Bindet die Aktivität an den Dienst
        }
        println("onStart() - MainActivity")                            // Debug-Nachricht
    }

    override fun performServiceAction(action: ServiceAction, vararg contacts: List<ContactReadyForSms>) { // Führt eine Aktion im Dienst aus
        if (isSmsServiceBound && action == ServiceAction.PushContact && contacts.isNotEmpty()) { // Prüft, ob der Dienst gebunden und die Aktion gültig ist
            val allContacts = mutableListOf<ContactReadyForSms>()                                // Liste für alle Kontakte
            contacts.forEach { contact ->                                                        // Fügt die Kontakte zur Liste hinzu
                allContacts.addAll(contact)
            }
            smsService.addMessageToQueue(allContacts)                  // Fügt die Kontakte zur Warteschlange des Dienstes hinzu
        }
    }

    override fun onResume() {                                          // Wird beim Fortsetzen der Aktivität aufgerufen
        super.onResume()
        checkAndRequestPermissions()
        println("onResume() - MainActivity")                           // Debug-Nachricht
    }

    override fun onPause() {                                           // Wird beim Pausieren der Aktivität aufgerufen
        super.onPause()
        println("onPause() - MainActivity")                            // Debug-Nachricht
    }

    override fun onStop() {                                            // Wird beim Stoppen der Aktivität aufgerufen
        super.onStop()
        if (isSmsServiceBound) {                                       // Prüft, ob der Dienst gebunden ist
            unbindService(smsServiceConnection)                        // Hebt die Dienstverbindung auf
            isSmsServiceBound = false                                  // Setzt den Verbindungsstatus auf "nicht gebunden"
        }
        println("onStop() - MainActivity")                             // Debug-Nachricht
    }

    override fun onDestroy() {                                         // Wird beim Zerstören der Aktivität aufgerufen
        super.onDestroy()
        contactBuffer.smsServiceInteractor = null                      // Entfernt die Verbindung zwischen Dienst und ViewModel
        println("onDestroy() - MainActivity")                          // Debug-Nachricht
    }

    private fun checkAndRequestPermissions() {                         // Überprüft und fordert erforderliche Berechtigungen an, bei start der Acticity.
        if (!isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_CONTACTS_READ
            )
        } else if (!isPermissionGranted(Manifest.permission.READ_CALENDAR)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR),
                REQUEST_CODE_CONTACTS_READ
            )
        } else if (!isPermissionGranted(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_CODE_SEND_SMS
            )
        } else {
            println("loadcontacts() called")
            contactBuffer.loadContacts(this)                          // Lädt Kontakte ins ViewModel
            contactBuffer.loadCalender(this)                          // Lädt Kalenderdaten ins ViewModel
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {    // Überprüft, ob eine Berechtigung erteilt wurde
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(                          // Wird aufgerufen, wenn eine Berechtigung angefordert wurde
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((requestCode == REQUEST_CODE_CONTACTS_READ || requestCode == REQUEST_CODE_KALENDER_READ) || requestCode == REQUEST_CODE_SEND_SMS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions()                              // Überprüft erneut die Berechtigungen
        }
    }
}





package com.example.meetingnotification.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.meetingnotification.ui.contact.ContactReadyForSms
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.services.ServiceAction
import com.example.meetingnotification.ui.services.SmsSendingService
import com.example.meetingnotification.ui.services.SmsSendingServiceInteractor

private val TAG = MainActivity::class.simpleName

class MainActivity : AppCompatActivity(), SmsSendingServiceInteractor {

    companion object { // Begleitendes statisches Objekt für Konfigurationskonstanten
        private const val REQUEST_CODE_FOR_ALL_NEEDED_PERMISSIONS = 1
    }

    private val contactBuffer by viewModels<ContactsSearchScreenViewModel> { AppViewModelProvider.Factory }     // ViewModel für die Kontaktsuche

    private lateinit var smsService: SmsSendingService                 // Später zu initialisierender SMS-Dienst
    private var isSmsServiceBound = false                              // Status, ob der SMS-Dienst gebunden ist

    private val smsServiceConnection =
        object : ServiceConnection {    // Objekt für die Verbindung zum SMS-Dienst
            override fun onServiceConnected(
                className: ComponentName,
                service: IBinder
            ) { // Wird aufgerufen, wenn die Verbindung hergestellt ist
                val binder = service as SmsSendingService.LocalBinder      // Holt den Binder vom SMS-Dienst
                smsService = binder.getService()                           // Holt die Dienstinstanz
                isSmsServiceBound = true                                   // Setzt den Verbindungsstatus auf "gebunden"

                val application = applicationContext as MeetingNotificationApplication
                smsService.initialize(
                    application.container.contactRepository,
                    application.container.eventRepository,
                    application.container.dateMessageSendRepository
                )
                Log.d(TAG,"serviceConnected() and Repositories initialized.")                              // Debug-Nachricht zur Bestätigung
            }

            override fun onServiceDisconnected(arg0: ComponentName) {      // Wird aufgerufen, wenn die Verbindung getrennt wird
                isSmsServiceBound = false                                  // Setzt den Verbindungsstatus auf "nicht gebunden"
                Log.d(TAG,"serviceDisconected()")
            }
        }


    //Action to push contacts in servive or to send message to all contacts or to get Contacts from ServiceQueue.
    override fun performServiceActionToAddOrSend(action: ServiceAction, contacts: List<ContactReadyForSms>)
    {
        if (isSmsServiceBound && action == ServiceAction.PushContact && contacts.isNotEmpty()) { // Prüft, ob der Dienst gebunden und die Aktion gültig ist. Action to insert Contact to Queue.
            val allContacts = mutableListOf<ContactReadyForSms>()                                // Liste für alle Kontakte
            contacts.forEach { contact ->                                                        // Fügt die Kontakte zur Liste hinzu
                allContacts.add(contact)
            }
            smsService.addMessageToQueue(allContacts)                  // Fügt die Kontakte zur Warteschlange des Dienstes hinzu
        } else if (isSmsServiceBound && action == ServiceAction.SendMessage) { //Action to Send Messages
            smsService.showMessageSendDialog(
                this@MainActivity,
                smsService.messageQueue.takeIf { it.isNotEmpty() }?.map { it.fullName }.toString()
            ) { accepted ->
                if (accepted) {
                    smsService.sendNextMessage(this@MainActivity)
                }
            }
        }
    }

    override fun performServiceActionToGetContactFromQueue(action: ServiceAction): List<Int> {
        return if (isSmsServiceBound && action == ServiceAction.GetContactsFromQueue)
            smsService.getContactsInSmsQueueWithId()
        else
            emptyList()
    }

    override fun performServiceActionToRemoveFromQueue(action: ServiceAction, contactId: Int) {
        if (action == ServiceAction.DeleteContactFromQueue){
            smsService.removeContactFromQueue(contactId)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.d(TAG,"onCreate() - MainActivity")

        val destinationWhenClickNotification = intent?.getStringExtra("destination")
        Log.d(TAG,"onCreate() - destination = $destinationWhenClickNotification")


        //checkAndRequestPermissions()                                   // Überprüft und fordert erforderliche Berechtigungen an
        contactBuffer.smsServiceInteractor = this                      // Verknüpft das ViewModel mit dem Dienst-Interface. Wichtige schnittstelle , weil im viewmodel kein service erstellt werden darf.

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NotificationApp(
                    viewModel = contactBuffer,
                    initialDestination = destinationWhenClickNotification
                )
            }
        }

        //Nötig um Intent zurückzusetzen, das diese Activity geöffnet hat (Notification Click), weil bei bilschirmrotation ansonsten die Route immer genutzt werden würde.
        intent?.replaceExtras(Bundle())
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG,"onRestart() - MainActivity")
    }

    override fun onStart() {                                           // Wird beim Start der Aktivität aufgerufen
        super.onStart()
        Log.d(TAG,"onStart() - MainActivity")                            // Debug-Nachricht
        checkAndRequestPermissions()

        Intent(this, SmsSendingService::class.java)
            .also { intent ->   // Erstellt einen Intent für den SMS-Dienst
            bindService(intent, smsServiceConnection, BIND_AUTO_CREATE)             // Bindet die Aktivität an den Dienst
        }
    }

    override fun onResume() {                                          // Wird beim Fortsetzen der Aktivität aufgerufen
        super.onResume()
        Log.d(TAG,"onResume() - MainActivity")                           // Debug-Nachricht
    }

    override fun onPause() {                                           // Wird beim Pausieren der Aktivität aufgerufen
        super.onPause()
        Log.d(TAG,"onPause() - MainActivity")                           // Debug-Nachricht
    }

    override fun onStop() {                                            // Wird beim Stoppen der Aktivität aufgerufen
        super.onStop()
        if (isSmsServiceBound) {                                       // Prüft, ob der Dienst gebunden ist
            unbindService(smsServiceConnection)                        // Hebt die Dienstverbindung auf
            isSmsServiceBound = false                                  // Setzt den Verbindungsstatus auf "nicht gebunden"
        }
        Log.d(TAG,"onStop() - MainActivity")                             // Debug-Nachricht
    }

    override fun onDestroy() {                                         // Wird beim Zerstören der Aktivität aufgerufen
        super.onDestroy()
        contactBuffer.smsServiceInteractor = null                      // Entfernt die Verbindung zwischen Dienst und ViewModel
        Log.d(TAG,"onDestroy() - MainActivity")                         // Debug-Nachricht
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        if (!isPermissionGranted(Manifest.permission.READ_CALENDAR)) {
            permissionsToRequest.add(Manifest.permission.READ_CALENDAR)
        }
        if (!isPermissionGranted(Manifest.permission.SEND_SMS)) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && !isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissionsToRequest.size == 1 && permissionsToRequest[0] == Manifest.permission.POST_NOTIFICATIONS) {
            contactBuffer.loadContactsWrapper(this)
            contactBuffer.loadCalender(this)
            Log.d(TAG, "Permissions granted for all except the Post_Notifications. Loading Calender and Contacts...")
        } else if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(), // Alle benötigten permissions in einem Array.
                REQUEST_CODE_FOR_ALL_NEEDED_PERMISSIONS //  Alle Permissions unter gleichem Code,
            )
        } else {
            // Nur wenn ALLE Berechtigungen erteilt wurden, App starten
            contactBuffer.loadContactsWrapper(this)
            contactBuffer.loadCalender(this)
            Log.d(TAG, "Permissions granted for all. Loading data...")
        }
    }

    private fun showPermissionExplanationDialog(ungrantedPermissions: List<String>) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_dialog_title))
            .setMessage(getString(R.string.permission_dialog_message))
            .setPositiveButton(getString(R.string.permission_dialog_positive)) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    ungrantedPermissions.toTypedArray(),
                    REQUEST_CODE_FOR_ALL_NEEDED_PERMISSIONS
                )
            }
            .setNegativeButton(getString(R.string.permission_dialog_negative), null)
            .show()
    }

    private fun isPermissionGranted(permission: String): Boolean {    // Überprüft, ob eine Berechtigung erteilt wurde
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_FOR_ALL_NEEDED_PERMISSIONS) {

            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }

            if (deniedPermissions.isEmpty() || (deniedPermissions.size == 1 && deniedPermissions[0] == Manifest.permission.POST_NOTIFICATIONS) ) {
                contactBuffer.loadContactsWrapper(this)
                contactBuffer.loadCalender(this)
                Log.d(TAG, "Alle Berechtigungen akzeptiert oder nur Benachrichtigungen abgelehnt")
            } else {
                val shouldShowRationale = deniedPermissions.any {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }

                if (shouldShowRationale) {
                    showPermissionExplanationDialog(deniedPermissions)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permission_permanently_denied_title))
                        .setMessage(getString(R.string.permission_permanently_denied_message))
                        .setPositiveButton(getString(R.string.permission_go_to_settings)) { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", packageName, null)
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.permission_dialog_negative), null)
                        .show()
                }
            }
        }
    }
}





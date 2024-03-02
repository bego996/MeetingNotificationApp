package com.example.meetingnotification.ui

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.example.meetingnotification.ui.Services.SmsSendingService
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel


class MainActivity : AppCompatActivity()  {
    private lateinit var smsService : SmsSendingService
    private var isBound = false

    private val contactBuffer by viewModels<ContactsSearchScreenViewModel> {AppViewModelProvider.Factory}

    companion object {
        private const val REQUEST_CODE_CONTACTS_READ = 1
        private const val REQUEST_CODE_KALENDER_READ = 2
        private const val REQUEST_CODE_SEND_SMS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        registerReceiver(smsSentReceiver, IntentFilter("SMS_SENT"))

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = Color.Green
            ) {
                NotificationApp(
                    viewModel = contactBuffer,
                    activateSendSmsReceiver = {startServiceAndBind()})
            }
        }
    }

    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (resultCode == Activity.RESULT_OK){
                if (isBound){
                    smsService.sendNextMessage(this@MainActivity)
                }
            }
        }
    }

    private fun startServiceAndBind() {
        val serviceIntent = Intent(this, SmsSendingService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SmsSendingService.LocalBinder
            smsService = binder.getService()
            isBound = true

            smsService.addMessageToQueue("036462723","Test1")
            smsService.addMessageToQueue("0364372","Test2")
            smsService.addMessageToQueue("0362733","Test3")

            if (isBound){
                smsService.sendNextMessage(this@MainActivity)
            }

            println("serviceConnected()")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        println("started")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        println("destroyed")
    }

    override fun onResume() {
        super.onResume()
        println("resumed")
    }

    override fun onStop() {
        super.onStop()
        println("paused")
    }

    override fun onPause() {
        super.onPause()
        println("paused")
    }

    private fun checkAndRequestPermissions(){
        if (!isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_READ)
        }else if (!isPermissionGranted(Manifest.permission.READ_CALENDAR)){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), REQUEST_CODE_CONTACTS_READ)
        }else if (!isPermissionGranted(Manifest.permission.SEND_SMS)){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_CODE_SEND_SMS)
        }
        else{
            contactBuffer.loadContacts(this)
            contactBuffer.loadCalender(this)
        }
    }

    private fun isPermissionGranted(permission : String) : Boolean{
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((requestCode == REQUEST_CODE_CONTACTS_READ || requestCode == REQUEST_CODE_KALENDER_READ) || requestCode == REQUEST_CODE_SEND_SMS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions()
        }
    }
}




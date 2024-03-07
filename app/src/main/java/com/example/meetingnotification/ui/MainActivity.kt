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
import com.example.meetingnotification.ui.Services.SmsSendingService
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel


class MainActivity : AppCompatActivity()  {
    companion object {
        private const val REQUEST_CODE_CONTACTS_READ = 1
        private const val REQUEST_CODE_KALENDER_READ = 2
        private const val REQUEST_CODE_SEND_SMS = 3
    }

    private val contactBuffer by viewModels<ContactsSearchScreenViewModel> {AppViewModelProvider.Factory}

    private lateinit var smsService : SmsSendingService
    private var isSmsServiceBound = false

    private val smsServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SmsSendingService.LocalBinder
            smsService = binder.getService()
            isSmsServiceBound = true
            println("serviceConnected()")
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isSmsServiceBound = false
            println("serviceDisconected()")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = Color.Green
            ) {
                NotificationApp(
                    viewModel = contactBuffer,
                    activateSendSmsReceiver = {/*startServiceAndBind()*/})
            }
        }
    }



    override fun onStart() {
        super.onStart()
        Intent(this,SmsSendingService::class.java).also { intent ->
            bindService(intent,smsServiceConnection, BIND_AUTO_CREATE)
        }
        println("onStart() - MainActivity")
    }
    override fun onResume() {
        super.onResume()
        println("resumed")
    }
    override fun onPause() {
        super.onPause()
        println("paused")
    }
    override fun onStop() {
        super.onStop()
        if (isSmsServiceBound) {
            unbindService(smsServiceConnection)
            isSmsServiceBound = false
        }
        println("onStop() - MainActivity")
    }
    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy() - MainActivity")
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




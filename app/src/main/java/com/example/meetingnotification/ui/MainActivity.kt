package com.example.meetingnotification.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel


class MainActivity : AppCompatActivity()  {

    private val contactBuffer by viewModels<ContactsSearchScreenViewModel> {AppViewModelProvider.Factory}

    companion object {
        private const val REQUEST_CODE_CONTACTS_READ = 1
        private const val REQUEST_CODE_KALENDER_READ = 2
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
                NotificationApp(viewModel = contactBuffer)
                //TemplateOverwatch()
            }
        }
    }


    private fun checkAndRequestPermissions(){
        if (!isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_READ)
        }else if (!isPermissionGranted(Manifest.permission.READ_CALENDAR)){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), REQUEST_CODE_CONTACTS_READ)
        }else{
            contactBuffer.loadContacts(this)
            contactBuffer.loadCalender(this)
        }
    }

    private fun isPermissionGranted(permission : String) : Boolean{
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((requestCode == REQUEST_CODE_CONTACTS_READ || requestCode == REQUEST_CODE_KALENDER_READ) && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions()
        }
    }
}




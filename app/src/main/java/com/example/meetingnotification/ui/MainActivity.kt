package com.example.meetingnotification.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.util.Log

import androidx.activity.viewModels
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.contact.SearchListScreen
import com.example.meetingnotification.ui.home.HomeScreenViewModel


class MainActivity : AppCompatActivity()  {

    val contactBuffer by viewModels<HomeScreenViewModel> {HomeScreenViewModel.Factory}


    companion object {
        private const val REQUEST_CODE_CONTACTS_READ = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_READ)
        }else{
            contactBuffer.loadContacts(this)
            contactBuffer.isLoaded = true
            Log.d("Loger","Contactbuffer is loaded = ${contactBuffer.isLoaded}")
        }

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = Color.Green
            ) {
                NotificationApp(viewModel = contactBuffer)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CONTACTS_READ && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            contactBuffer.loadContacts(this)
        }
    }
}




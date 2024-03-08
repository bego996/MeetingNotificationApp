package com.example.meetingnotification.ui.Services

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

class SmsSendingService : Service() {
    private lateinit var receiver: SmsSentReceiver
    private val binder = LocalBinder()
    var messageQueue = ArrayDeque<SmsMessage>()

    inner class LocalBinder : Binder() {
        fun getService(): SmsSendingService = this@SmsSendingService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        println("ServiceOnCreate()")
        receiver = SmsSentReceiver(this)
        val intentFilter = IntentFilter("SMS_SENT")
        registerReceiver(receiver, intentFilter)
        println("Receiver is registered")
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun addMessageToQueue(phoneNumber: String, message: String, fullName: String) {
        messageQueue.add(SmsMessage(phoneNumber, message, fullName))
    }

     fun sendNextMessage(context: Context) {
        println("sendNextMessage is called()")
        if (messageQueue.isNotEmpty()) {
            val nextMessage = messageQueue.removeFirst()

            val smsIntent = PendingIntent.getBroadcast(
                context, 0, Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java).sendTextMessage(
                    nextMessage.phoneNumber,
                    null,
                    nextMessage.message,
                    smsIntent,
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

    fun showMessageSendDialog(context: Context, fullName: String, onResult: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("SmS Send Request")
        builder.setMessage("Do you want to send the notification Message to this contacts : \n$fullName ?")

        builder.setPositiveButton("Accept") { dialog, which ->
            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
            onResult(true)
        }

        builder.setNegativeButton("Deny") { dialog, which ->
            Toast.makeText(context, "Denied", Toast.LENGTH_SHORT).show()
            onResult(false)
        }

        builder.create().show()
    }

    data class SmsMessage(val phoneNumber: String, val message: String, val fullName: String)
}


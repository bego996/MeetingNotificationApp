package com.example.meetingnotification.ui.Services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.telephony.SmsManager

class SmsSendingService: Service() {
    private val binder = LocalBinder()
    private var messageQueue = ArrayDeque<SmsMessage>()
    private lateinit var smsSentReceiver : BroadcastReceiver
    inner class LocalBinder : Binder(){
        fun getService(): SmsSendingService = this@SmsSendingService
    }
    override fun onBind(intent: Intent): IBinder {
       return binder
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        println("receiver onCreate started")
        smsSentReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                if (resultCode == Activity.RESULT_OK){
                    sendNextMessage(context)
                }
            }
        }

        val intentFilter = IntentFilter("SMS_SENT")
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(smsSentReceiver,intentFilter)
        println("Receiver is registered")
    }
    override fun onDestroy() {
        unregisterReceiver(smsSentReceiver)
        super.onDestroy()
    }

    fun addMessageToQueue(phoneNumber: String,message: String){
        messageQueue.add(SmsMessage(phoneNumber,message))
    }

    fun sendNextMessage(context: Context){
        println("sendNextMessage is called()")
        if (messageQueue.isNotEmpty()){
            val nextMessage = messageQueue.removeFirst()

            val smsIntent = PendingIntent.getBroadcast(context,0,Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE)

            SmsManager.getDefault().sendTextMessage(nextMessage.phoneNumber,null,nextMessage.message,smsIntent,null)
        }
    }

    data class SmsMessage(val phoneNumber: String, val message: String)
}


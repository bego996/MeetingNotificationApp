package com.example.meetingnotification.ui.Services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.telephony.SmsManager
import com.example.meetingnotification.ui.TBroadcastReceiver.SmsSentReceiver

class SmsSendingService: Service() {
    private lateinit var receiver : SmsSentReceiver
    private val binder = LocalBinder()
    private var messageQueue = ArrayDeque<SmsMessage>()
    inner class LocalBinder : Binder(){
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
        registerReceiver(receiver,intentFilter)
        println("Receiver is registered")
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun addMessageToQueue(phoneNumber: String,message: String){
        messageQueue.add(SmsMessage(phoneNumber,message))
    }

    fun sendNextMessage(context: Context){
        println("sendNextMessage is called()")
        if (messageQueue.isNotEmpty()){
            val nextMessage = messageQueue.removeFirst()

            val smsIntent = PendingIntent.getBroadcast(context,0,Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE)

            SmsManager.getDefault().sendDataMessage(nextMessage.phoneNumber,null,1025,nextMessage.message.toByteArray(Charsets.UTF_8),smsIntent,null)
            //SmsManager.getDefault().sendTextMessage(nextMessage.phoneNumber,null,nextMessage.message,smsIntent,null) depreceated!
        }
    }
    data class SmsMessage(val phoneNumber: String, val message: String)
}


package com.example.meetingnotification.ui.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.meetingnotification.ui.Services.SmsSendingService

private val TAG = SmsSentReceiver::class.simpleName
class SmsSentReceiver(private val service : SmsSendingService) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"smsReceiverCalled()")
        if (resultCode == Activity.RESULT_OK){
            service.sendNextMessage(context)
        }
    }
}
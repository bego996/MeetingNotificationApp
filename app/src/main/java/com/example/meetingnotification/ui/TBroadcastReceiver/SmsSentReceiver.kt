package com.example.meetingnotification.ui.TBroadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("broadcastReceiver0","onReceiveCalled")
        if (resultCode == Activity.RESULT_OK){
            val sendMessageIntent = Intent("SEND_SEND")
            context.sendBroadcast(sendMessageIntent)
        }
    }
}
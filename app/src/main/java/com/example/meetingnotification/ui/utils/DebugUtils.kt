package com.example.meetingnotification.ui.utils

import android.util.Log
import com.example.meetingnotification.ui.BuildConfig
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

object DebugUtils {

    inline fun logExecutionTime(tag: String? = "Performance", blockName: String = "CodeBlock", block: () -> Unit) {
        if (BuildConfig.DEBUG) {
            val time = measureTimeMillis {
                block()
            }.milliseconds
            Log.d(tag, "$blockName took $time")
        } else {
            block()
        }
    }
}
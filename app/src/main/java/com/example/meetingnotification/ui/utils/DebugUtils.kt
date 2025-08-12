package com.example.meetingnotification.ui.utils

import android.util.Log
import com.example.meetingnotification.ui.BuildConfig
import com.google.firebase.perf.FirebasePerformance
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

//Used to meassure method execution times. Has 2 states, one for debug builds and one for release.
object DebugUtils {
    inline fun logExecutionTime(tag: String? = "Performance", blockName: String = "CodeBlock", block: () -> Unit) {
        if (BuildConfig.DEBUG) {
            val time = measureTimeMillis {
                block()
            }.milliseconds
            Log.d(tag, "$blockName took $time")
        } else {
            val myTrace = FirebasePerformance.getInstance().newTrace(blockName)
            myTrace.start()
            block()
            myTrace.stop()
        }
    }
}
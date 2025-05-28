package com.example.meetingnotification.ui.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.meetingnotification.ui.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InstructionReadRepository(private val context: Context) {

    private var instructionAllreadyReaden = false

    private val instructionKey = booleanPreferencesKey("instruction_readen_id")

    suspend fun instructionReaden(){
        context.dataStore.edit { settings ->
            settings[instructionKey] = true
        }
    }

    fun get(): Flow<Boolean>{
        return context.dataStore.data
            .map { preferences ->
                preferences[instructionKey] ?: instructionAllreadyReaden
            }
    }

}
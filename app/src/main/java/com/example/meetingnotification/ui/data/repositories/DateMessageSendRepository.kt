package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.entities.DateMessageSent
import kotlinx.coroutines.flow.Flow

interface DateMessageSendRepository {

    suspend fun insert(lastSendet: DateMessageSent)

    suspend fun delete(lastSendet: DateMessageSent)

    fun getLastSendetInfos(): Flow<DateMessageSent?>
}
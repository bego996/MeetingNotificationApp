package com.simba.meetingnotification.ui.data.repositories

import com.simba.meetingnotification.ui.data.entities.DateMessageSent
import kotlinx.coroutines.flow.Flow

interface DateMessageSendRepository {

    suspend fun insert(lastSendet: DateMessageSent)

    suspend fun delete(lastSendet: DateMessageSent)

    fun getLastSendetInfos(): Flow<DateMessageSent?>
}
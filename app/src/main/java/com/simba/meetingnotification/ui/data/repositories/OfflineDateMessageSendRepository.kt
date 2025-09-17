package com.simba.meetingnotification.ui.data.repositories

import com.simba.meetingnotification.ui.data.dao.MessageSendDao
import com.simba.meetingnotification.ui.data.entities.DateMessageSent
import kotlinx.coroutines.flow.Flow

class OfflineDateMessageSendRepository(private val messageSendDao: MessageSendDao): DateMessageSendRepository {
    override suspend fun insert(lastSendet: DateMessageSent) {
        messageSendDao.insert(lastSendet)
    }

    override suspend fun delete(lastSendet: DateMessageSent) {
        messageSendDao.delete(lastSendet)
    }

    override fun getLastSendetInfos(): Flow<DateMessageSent?> = messageSendDao.getLastSendetInfos()
}
package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.dao.EventDao
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.EventWithContact
import kotlinx.coroutines.flow.Flow

class OfflineEventRepository(private val eventDao: EventDao) : EventRepository {

    override fun getEventWithContact(eventId: Int): Flow<EventWithContact> {
        return eventDao.getEventWithContact(eventId)
    }

    override suspend fun insertItem(event: Event) {
        eventDao.insert(event)
    }

    override suspend fun deleteItem(event: Event) {
        eventDao.delete(event)
    }

    override suspend fun updateItem(event: Event) {
        eventDao.update(event)
    }

}
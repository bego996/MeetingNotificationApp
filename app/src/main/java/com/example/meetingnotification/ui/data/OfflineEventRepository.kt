package com.example.meetingnotification.ui.data

import kotlinx.coroutines.flow.Flow

class OfflineEventRepository(private val eventDao: EventDao) : EventRepository {

    override fun getEventsForContactStream(contactId: Int): Flow<List<Event>> {
        return eventDao.getEventsForContact(contactId)
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
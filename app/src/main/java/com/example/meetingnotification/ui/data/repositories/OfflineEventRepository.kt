package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.dao.EventDao
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.EventWithContact
import kotlinx.coroutines.flow.Flow

class OfflineEventRepository(private val eventDao: EventDao) : EventRepository {

    override fun getEventWithContact(eventId: Int): Flow<EventWithContact> {
        return eventDao.getEventWithContact(eventId)
    }

    override fun getNotNotifiedEventsAndFromActualDateTime(dateNow: String, dateTo: String): Int {
        return eventDao.getNotNotifiedEventsAndFromActualDateTime(dateNow,dateTo)
    }

    override fun getEventFromDateAndTimeParam(eventDate: String, eventTime: String): Flow<List<Event>> {
        return eventDao.getEventFromDateAndTimeParam(eventDate,eventTime)
    }

    override suspend fun getAllEventsStream(): List<Event> {
        return eventDao.getAllEventsStream()
    }

    override suspend fun getEventsAfterToday(dateNow: String): List<Event> {
        return eventDao.getEventsAfterToday(dateNow)
    }

    override suspend fun getEvents(contactOwnerId: Int): List<Event> {
        return eventDao.getEvents(contactOwnerId)
    }

    override suspend fun insertItem(event: Event) {
        eventDao.insert(event)
    }

    override suspend fun insertAllEvents(events: List<Event>) {
        eventDao.insertAll(events)
    }

    override suspend fun deleteItem(event: Event) {
        eventDao.delete(event)
    }

    override suspend fun updateItem(event: Event) {
        eventDao.update(event)
    }

}
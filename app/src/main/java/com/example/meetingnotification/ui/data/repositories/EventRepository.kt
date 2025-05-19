package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.EventWithContact
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    fun getEventWithContact(eventId: Int): Flow<EventWithContact>

    fun getNotNotifiedEventsAndFromActualDateTime(dateNow: String,dateTo: String): Int

    fun getEventFromDateAndTimeParam(eventDate: String,eventTime: String): Flow<List<Event>>

    //Nur für testzweck aktivieren.
    suspend fun getAllEventsStream(): List<Event>


    suspend fun getEventsAfterToday(dateNow: String): List<Event>

    suspend fun getEvents(contactOwnerId: Int): List<Event>



    /**
     * Insert item in the data source
     */
    suspend fun insertItem(event: Event)

    suspend fun insertAllEvents(events: List<Event>)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(event: Event)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(event: Event)
}
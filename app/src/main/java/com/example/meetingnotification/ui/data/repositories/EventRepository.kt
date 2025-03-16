package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.EventWithContact
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    fun getEventWithContact(eventId: Int): Flow<EventWithContact>

    fun getEventFromDateAndTimeParam(eventDate: String,eventTime: String): Flow<List<Event>>

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(event: Event)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(event: Event)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(event: Event)
}
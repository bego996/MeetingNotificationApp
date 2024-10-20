package com.example.meetingnotification.ui.data

import kotlinx.coroutines.flow.Flow

interface EventRepository {

    fun getEventsForContactStream(contactId: Int): Flow<List<Event>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */

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
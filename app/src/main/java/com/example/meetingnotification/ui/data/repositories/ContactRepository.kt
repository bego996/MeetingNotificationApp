package com.example.meetingnotification.ui.data.repositories

import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.relations.ContactWithEvents
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContactsStream(): Flow<List<Contact>>

    fun getContactWithEvents(contactId: Int): Flow<ContactWithEvents>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    suspend fun getContactStream(id: Int): Contact?


    /**
     * Insert item in the data source
     */
    suspend fun insertItem(contact: Contact)

    suspend fun updateAll(contacts: List<Contact>)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(contact: Contact)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(contact: Contact)
}
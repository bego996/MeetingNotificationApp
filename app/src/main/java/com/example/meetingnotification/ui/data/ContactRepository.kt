package com.example.meetingnotification.ui.data

import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContactsStream(): Flow<List<Contact>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getContactStream(id: Int): Flow<Contact?>

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(contact: Contact)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(contact: Contact)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(contact: Contact)
}
package com.example.meetingnotification.ui.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.relations.ContactWithEvents
import kotlinx.coroutines.flow.Flow


@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Update
    suspend fun updateAll(contacts: List<Contact>)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * from contacts WHERE id = :id ")
    suspend fun getContact(id: Int): Contact?

    @Query("SELECT * from contacts ORDER BY firstName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactWithEvents(contactId: Int): Flow<ContactWithEvents>
}
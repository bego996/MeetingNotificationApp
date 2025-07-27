package com.example.meetingnotification.ui.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.meetingnotification.ui.data.entities.Event
import com.example.meetingnotification.ui.data.relations.EventWithContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) //Wenn ein Konflikt wie zb Contraintsbruch erfolgt, dann wird der problematische Datensatz ignoriert und nicht eingefügt.Die Transaction geht weiter.
    suspend fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<Event>)

    @Update //Auch hier oder bei delete könnte man eine OnconflictStrategy auswählen. Merken: Abort Strategie ist immer Default wert bei Update,Insert,Delete.
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Delete
    suspend fun deleteExpiredEvents(expiredEvents: List<Event>)

    //TEST
    @Query("SELECT * from events where eventDate >= :dateNow ")
    suspend fun getEventsAfterToday(dateNow: String): List<Event>

    @Query("SELECT * FROM events WHERE eventDate < :dateNow")
    suspend fun getExpiredEvents(dateNow: String) : List<Event>

    @Query("SELECT * from events where contactOwnerId = :contactOwnerId")
    suspend fun getEvents(contactOwnerId: Int): List<Event>

    @Transaction
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    fun getEventWithContact(eventId: Int): Flow<EventWithContact>

    @Query("SELECT COUNT(*) FROM events WHERE eventDate BETWEEN :dateNow and :dateTo AND isNotified == 0")
    fun getNotNotifiedEventsAndFromActualDateTime(dateNow: String,dateTo: String): Int

    @Query("SELECT * FROM events WHERE eventDate BETWEEN :dateNow and :dateTo AND isNotified == 0")
    suspend fun getAllEventsAndFromActualDateTime(dateNow: String,dateTo: String): List<Event>


    @Query("SELECT * FROM events WHERE eventDate = :eventDate AND eventTime = :eventTime")
    fun getEventFromDateAndTimeParam(eventDate: String,eventTime: String): Flow<List<Event>>

    //Nur für testzweck aktivieren.
    @Query("SELECT * FROM events ORDER BY eventId")
    suspend fun getAllEventsStream(): List<Event>
}
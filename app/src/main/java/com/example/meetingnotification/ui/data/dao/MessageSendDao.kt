package com.example.meetingnotification.ui.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meetingnotification.ui.data.entities.DateMessageSent
import kotlinx.coroutines.flow.Flow


@Dao
interface MessageSendDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lastSendet: DateMessageSent)

    @Delete
    suspend fun delete(lastSendet: DateMessageSent)

    @Query("SELECT * from dateMessagesSend ORDER BY lastDateSendet DESC, lastTimeSendet DESC LIMIT 1")
    fun getLastSendetInfos(): Flow<DateMessageSent?>
}
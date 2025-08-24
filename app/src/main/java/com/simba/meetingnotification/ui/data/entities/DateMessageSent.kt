package com.simba.meetingnotification.ui.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "dateMessagesSend")
data class DateMessageSent (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val lastTimeSendet: String,
    val lastDateSendet: String
)

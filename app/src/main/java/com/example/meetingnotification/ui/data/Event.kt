package com.example.meetingnotification.ui.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = Contact::class,
        parentColumns = ["id"],
        childColumns = ["contactOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["contactOwnerId"])]
    )
data class Event (
    @PrimaryKey(autoGenerate = false)
    val eventId: Int = 0,
    val eventDate: String,
    val contactOwnerId: Int,
    var isNotified: Boolean = false
)
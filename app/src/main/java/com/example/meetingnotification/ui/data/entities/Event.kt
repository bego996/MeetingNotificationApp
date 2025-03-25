package com.example.meetingnotification.ui.data.entities

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
    indices = [
        Index(value = ["contactOwnerId"]), //Mit indices wird abfrage geschwindigkeit mit zb select verbessert auch die foreignkey abfragen sind schneller und auch zb Joins zwischen den Tabellen.
        Index(value = ["eventDate", "eventTime", "contactOwnerId"], unique = true)] //Unique constraint!
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val eventId: Int = 0,
    val eventDate: String,
    val eventTime: String,
    val contactOwnerId: Int,
    val isNotified: Boolean = false
)
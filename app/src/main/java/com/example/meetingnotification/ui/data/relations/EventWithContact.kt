package com.example.meetingnotification.ui.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.Event


data class EventWithContact(
    @Embedded val event: Event,       // `@Embedded` fügt die Felder von Event direkt ein
    @Relation(
        parentColumn = "contactOwnerId", // Verknüpft `contactOwnerId` von `Event`
        entityColumn = "id"              // Mit der `id`-Spalte von `Contact`
    )
    val contact: Contact               // Der zugehörige Kontakt
)

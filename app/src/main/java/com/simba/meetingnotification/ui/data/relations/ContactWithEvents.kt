package com.simba.meetingnotification.ui.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.simba.meetingnotification.ui.data.entities.Contact
import com.simba.meetingnotification.ui.data.entities.Event

data class ContactWithEvents(
    @Embedded val contact: Contact,   // `@Embedded` fügt die Felder von Contact direkt ein
    @Relation(
        parentColumn = "id",          // Verknüpft die `id`-Spalte von `Contact`
        entityColumn = "contactOwnerId" // Mit der `contactOwnerId`-Spalte von `Event`
    )
    val events: List<Event>           // Eine Liste der zugehörigen Events
)

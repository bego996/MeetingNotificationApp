package com.example.meetingnotification.ui.data

import android.content.Context
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.DateMessageSendRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import com.example.meetingnotification.ui.data.repositories.OfflineContactRepository
import com.example.meetingnotification.ui.data.repositories.OfflineDateMessageSendRepository
import com.example.meetingnotification.ui.data.repositories.OfflineEventRepository

interface AppContainer {    // Schnittstelle, die ein App-Container-Konzept definiert
    val contactRepository: ContactRepository    // Deklariert eine Eigenschaft, die auf ein Kontakt-Repository verweist
    val eventRepository: EventRepository
    val dateMessageSendRepository: DateMessageSendRepository
}

class AppDataContainer(private val context: Context) : AppContainer {   // Implementierung der AppContainer-Schnittstelle, um ein Daten-Repository zu verwalten

    // Implementiert die contactRepository-Eigenschaft aus der Schnittstelle.
    override val contactRepository: ContactRepository by lazy {    // Nutzt `by lazy`, um die Eigenschaft nur dann zu initialisieren, wenn sie das erste Mal verwendet wird.
        // Verwendet das OfflineContactRepository, das eine Instanz von `ContactDao` (dem Datenzugriff-Objekt) benötigt.
        OfflineContactRepository(ContactDatabase.getDatabase(context).contactDao())     // Die Datenbank wird durch `getDatabase` erstellt oder abgerufen, und das DAO wird über die Methode `contactDao` geholt.
    }
    override val eventRepository: EventRepository by lazy{
        OfflineEventRepository(ContactDatabase.getDatabase(context).eventDAO())
    }

    override val dateMessageSendRepository: DateMessageSendRepository by lazy {
        OfflineDateMessageSendRepository(ContactDatabase.getDatabase(context).messageSendDAO())
    }

}
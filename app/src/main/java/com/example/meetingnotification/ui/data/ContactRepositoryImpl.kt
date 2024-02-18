package com.example.meetingnotification.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ContactRepositoryImpl : ContactRepository {

    // Dummy-Kontaktliste
    private val dummyContacts = listOf(
        Contact(1, "Mr", "John", "Doe", 'M', "1234567890"),
        Contact(2, "Ms", "Jane", "Doe", 'F', "0987654321")
        // Fügen Sie weitere Dummy-Kontakte nach Bedarf hinzu
    )

    override fun getAllContactsStream(): Flow<List<Contact>> {
        return flowOf(dummyContacts)
    }

    override fun getContactStream(id: Int): Flow<Contact?> {
        val contact = dummyContacts.firstOrNull { it.id == id }
        return flowOf(contact)
    }

    override suspend fun insertItem(contact: Contact) {
        // Für Dummy-Daten vielleicht nichts tun oder Log-Ausgabe
    }

    override suspend fun deleteItem(contact: Contact) {
        // Für Dummy-Daten vielleicht nichts tun oder Log-Ausgabe
    }

    override suspend fun updateItem(contact: Contact) {
        // Für Dummy-Daten vielleicht nichts tun oder Log-Ausgabe
    }

}

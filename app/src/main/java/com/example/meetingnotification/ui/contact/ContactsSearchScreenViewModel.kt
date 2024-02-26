package com.example.meetingnotification.ui.contact

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CalendarContract
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


class ContactsSearchScreenViewModel(
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val contactsUiState: StateFlow<ContactsUiState2> =
        contactRepository.getAllContactsStream().map { ContactsUiState2(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ContactsUiState2()
            )

    private val contactsWriteOnly = MutableLiveData<List<Contact>>()
    private val contactsReadOnly: LiveData<List<Contact>> get() = contactsWriteOnly

    private var calenderEvents = listOf<EventDateTitle>()


    suspend fun addContactsToDatabase(contactList: List<Contact>, compareIds: List<Int>) {
        for (id in compareIds) {
            contactList.firstOrNull { contact -> contact.id == id }?.let { matchingContact ->
                contactRepository.insertItem(matchingContact)
            }
        }
    }

    @SuppressLint("Range")
    fun loadContacts(context: Context) {
        val contactList = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        // Abfrage der Kontakte
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME.split(" ")[0] + " ASC"
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val fullname =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        .split(" ")

                val firstname = fullname[0]
                val surname = fullname[if (fullname.size > 2) 2 else 1]
                val sex = if (fullname.size > 2) fullname[1] else "X"

                val orgCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                    null
                )
                var title = "none"
                if (orgCursor != null && orgCursor.moveToFirst()) {
                    title =
                        orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))
                }
                orgCursor?.close()

                // Abfrage der Telefonnummern, falls vorhanden
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (phoneCursor?.moveToNext() == true) {
                        val phoneNumber = phoneCursor.getString(
                            phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        val isMale = sex.lowercase() == "m"
                        val defaultMessage = context.resources.getString(
                            R.string.defaultMessage,
                            if (isMale) "r" else "",
                            if (isMale) "Herr" else "Frau",
                            if (title != "none") "$title " else "",
                            surname,
                            "25.3",
                            "13:20"
                        )
                        contactList.add(
                            Contact(
                                id.toInt(),
                                title,
                                firstname,
                                surname,
                                sex[0].uppercaseChar(),
                                phoneNumber,
                                defaultMessage
                            )
                        )  // 'Contact' ist Ihre Datenklasse
                    }
                    phoneCursor?.close()
                }
            }
            cursor.close()
        }
        contactsWriteOnly.postValue(contactList)
    }

    @SuppressLint("Range")
    fun loadCalender(context: Context) {
        val eventList = mutableListOf<EventDateTitle>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            null,
            null,
            null,
            CalendarContract.Events.TITLE + " ASC"
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE))

                val startTimeMilis =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))

                val startEvent = Instant.ofEpochMilli(startTimeMilis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                eventList.add(
                    EventDateTitle(
                        startEvent,
                        title
                    )
                )
            }
            cursor.close()
        }
        calenderEvents = eventList
    }

    fun getContacts(): LiveData<List<Contact>> {
        return contactsReadOnly
    }

    fun getCalender() : List<EventDateTitle>{
        return calenderEvents
    }
}

data class MutablePairs(var first: Int, var second: Boolean)
data class ContactsUiState2(val contactList: List<Contact> = listOf())

data class EventDateTitle(val eventDate: LocalDateTime, val eventName: String)
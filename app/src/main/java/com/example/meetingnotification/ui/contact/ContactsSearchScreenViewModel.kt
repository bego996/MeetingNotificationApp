package com.example.meetingnotification.ui.contact

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.meetingnotification.ui.MeetingNotificationApplication
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.data.ContactRepository
import com.example.meetingnotification.ui.home.HomeScreenViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ContactsSearchScreenViewModel(contactRepository: ContactRepository) : ViewModel() {

    var contacts = MutableLiveData<List<Contact>>()

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                return ContactsSearchScreenViewModel(
                    (application as MeetingNotificationApplication).myRepository,
                ) as T
            }
        }
    }

    val contactsUiState: StateFlow<ContactsUiState2> =
        contactRepository.getAllContactsStream().map { ContactsUiState2(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ContactsUiState2()
            )

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
                        contactList.add(
                            Contact(
                                id.toInt(),
                                title,
                                firstname,
                                surname,
                                sex[0].uppercaseChar(),
                                phoneNumber
                            )
                        )  // 'Contact' ist Ihre Datenklasse
                    }
                    phoneCursor?.close()
                }
            }
            cursor.close()
        }
        contacts.postValue(contactList)
    }

    fun getContacts(): LiveData<List<Contact>> {
        return contacts
    }


    /*
        suspend fun addContactToDatabase(contact: Contact){
            contactRepository.insertItem(contact)
        }
     */


}


data class ContactsUiState2(val contactList: List<Contact> = listOf())
package com.example.meetingnotification.ui.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val firstName: String,
    val lastName: String,
    val sex: Char,
    val phone: String,
    val message: String
) : Comparable<Contact>{
    override fun compareTo(other: Contact): Int {
        return compareValuesBy(this,other,{it.firstName},{it.lastName})
    }
}

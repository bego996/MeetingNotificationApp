package com.example.meetingnotification.ui.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao() : ContactDao

    companion object{
        @Volatile
        private var Instance : ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {
            return Instance ?: synchronized(this){
                Room.databaseBuilder(context, ContactDatabase::class.java,"contact_database")
                    .build()
                    .also { Instance = it}
            }
        }
    }
}
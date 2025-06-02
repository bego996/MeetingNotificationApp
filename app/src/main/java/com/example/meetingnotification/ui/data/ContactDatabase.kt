package com.example.meetingnotification.ui.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meetingnotification.ui.data.dao.ContactDao
import com.example.meetingnotification.ui.data.dao.EventDao
import com.example.meetingnotification.ui.data.dao.MessageSendDao
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.data.entities.DateMessageSent
import com.example.meetingnotification.ui.data.entities.Event


// @Database: Markiert diese Klasse als Room-Datenbank
// entities = [Contact::class]: Enthält die Entität `Contact` (Tabelle in der Datenbank)
// version = 2: Setzt die aktuelle Version der Datenbank auf 2
// exportSchema = false: Verhindert, dass das Datenbankschema als JSON-Datei exportiert wird
@Database(entities = [Contact::class, Event::class, DateMessageSent::class], version = 21, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao() : ContactDao  // Deklariert eine abstrakte Methode, die eine Instanz des DAO (Data Access Object) für "Contact" zurückgibt.
    abstract fun eventDAO() : EventDao
    abstract fun messageSendDAO(): MessageSendDao

    // Ein Volatile-Attribut (flüchtig) stellt sicher, dass Änderungen an dieser Variablen sofort sichtbar sind, auch bei Zugriffen von mehreren Threads.
    companion object { @Volatile private var Instance : ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {    // Diese Methode ermöglicht den Zugriff auf die einzige Instanz der Datenbank.
            return Instance ?: synchronized(this) {     // Überprüft, ob die Instanz bereits initialisiert wurde.
                Room.databaseBuilder(context.applicationContext, ContactDatabase::class.java, "contact_database")      // Wenn noch nicht vorhanden, synchronisiert der Block den Zugriff und initialisiert die Instanz.
                    .fallbackToDestructiveMigration()   // Nutzt die destruktive Migration als Fallback. Das bedeutet, dass bei einem Versionskonflikt die Datenbank gelöscht und neu erstellt wird.
                    .build()    // Baut die Datenbank-Instanz und weist sie der `Instance`-Variablen zu.
                    .also { Instance = it }
            }
        }
    }
}
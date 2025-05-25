package com.example.meetingnotification.ui.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BackgroundImageManagerRepository(private val context: Context) {

    private val picturesInRessource: Array<Int> = arrayOf(
        R.drawable.background_picture_1,
        R.drawable.background_picture_2,
        R.drawable.background_picture_4,
        R.drawable.background_picture_5,
    )

    private val backgroundImageKey = intPreferencesKey("background_image_id")

    suspend fun save() {
        context.dataStore.edit { settings ->

            var pictureIndex = picturesInRessource.indexOf(get().first())

            if (pictureIndex in 0..2) {
                pictureIndex += 1
            } else {
                pictureIndex = 0
            }

            settings[backgroundImageKey] = picturesInRessource[pictureIndex]
        }
    }

    fun get(): Flow<Int> {
        return context.dataStore.data
            .map { preferences ->
                preferences[backgroundImageKey] ?: picturesInRessource[0]
            }
    }
}
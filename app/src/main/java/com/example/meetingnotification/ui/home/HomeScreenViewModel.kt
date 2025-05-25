package com.example.meetingnotification.ui.home


import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.repositories.BackgroundImageManagerRepository
import com.example.meetingnotification.ui.data.repositories.DateMessageSendRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val TAG = HomeScreenViewModel::class.simpleName

class HomeScreenViewModel(
    dateMessageSendRepository: DateMessageSendRepository,
    resources: Resources,
    private val backgroundImageManagerRepository: BackgroundImageManagerRepository
) : ViewModel() {

    //Resources for strings and so on, direct from the factory injected.
    val resourcesState = resources

    val selectedBackgroundPictureId: StateFlow<Int> =
        backgroundImageManagerRepository.get()
            .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),R.drawable.background_picture_1)

    fun changeDefaultImageInDatastore(){
        viewModelScope.launch {
            backgroundImageManagerRepository.save()
        }
    }

    val dateMessageSendUiState: StateFlow<MessageSendDateTimeUiState> =
        // StateFlow zur Überwachung des UI-Zustands der Kontakte. Für events ist kein zur Überwachung nötig. Ich kann auch so insert,delete und updaten von events.
        dateMessageSendRepository.getLastSendetInfos()
            .map { data ->
                MessageSendDateTimeUiState(
                lastTimeSendet = data?.lastTimeSendet ?: "",
                lastDateSendet = data?.lastDateSendet ?: ""
            )} // Wandelt die Daten in das UI-Format um
            .stateIn(
                scope = viewModelScope,                       // Coroutine-Bereich des ViewModels für Nebenläufigkeit
                started = SharingStarted.WhileSubscribed(5_000L), // Teilt die Daten weiter, solange abonniert
                initialValue = MessageSendDateTimeUiState()            // Anfangszustand der Kontakte ist eine leere Liste
            )
}

data class MessageSendDateTimeUiState(val lastTimeSendet:String = "",val lastDateSendet:String = "")
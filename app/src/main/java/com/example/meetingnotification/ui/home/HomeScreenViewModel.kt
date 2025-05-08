package com.example.meetingnotification.ui.home


import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.repositories.ContactRepository
import com.example.meetingnotification.ui.data.repositories.DateMessageSendRepository
import com.example.meetingnotification.ui.data.repositories.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val TAG = HomeScreenViewModel::class.simpleName

class HomeScreenViewModel(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val dateMessageSendRepository: DateMessageSendRepository,
    private val resources: Resources
) : ViewModel() {

    //Resources for strings and so on, direct from the factory injected.
    val resourcesState = resources

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
package com.example.meetingnotification.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingnotification.ui.data.repositories.InstructionReadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InstructionsScreenViewModel(private val instructionReadRepository: InstructionReadRepository) : ViewModel(){

    val instructionReadState: StateFlow<Boolean> =
        instructionReadRepository.get()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),false)

    fun setInstructionToReaden(){
        viewModelScope.launch {
            instructionReadRepository.instructionReaden()
        }
    }

}
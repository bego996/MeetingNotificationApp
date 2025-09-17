package com.simba.meetingnotification.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simba.meetingnotification.ui.data.repositories.InstructionReadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InstructionsScreenViewModel(private val instructionReadRepository: InstructionReadRepository) : ViewModel(){

    //region Properties
    val instructionReadState: StateFlow<Boolean> =
        instructionReadRepository.get()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),false)
    //endregion

    //region Methods
    fun setInstructionToReaden(){
        viewModelScope.launch {
            instructionReadRepository.instructionReaden()
        }
    }
    //endregion
}
package com.kaleyra.collaboration_suite_phone_ui.call.ringing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.RecordingMapper.toRecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.precall.viewmodel.PreCallViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class RingingViewModel(configure: suspend () -> Configuration): PreCallViewModel<RingingUiState>(configure) {

    override fun initialState() = RingingUiState()

    init {
        call
            .toRecordingTypeUi()
            .onEach { rec -> _uiState.update { it.copy(recording = rec) } }
            .launchIn(viewModelScope)

        call
            .toCallStateUi()
            .filterIsInstance<CallStateUi.Ringing>()
            .onEach { state -> _uiState.update { it.copy(answered = state.isConnecting) } }
            .launchIn(viewModelScope)

        call
            .amIWaitingOthers()
            .onEach { amIWaitingOthers -> _uiState.update { it.copy(amIWaitingOthers = amIWaitingOthers) } }
            .launchIn(viewModelScope)
    }

    fun accept() {
        call.getValue()?.connect()
    }

    fun decline() {
        call.getValue()?.end()
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RingingViewModel(configure) as T
                }
            }
    }

}
package com.kaleyra.video_sdk.call.ringing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingTypeUi
import com.kaleyra.video_sdk.call.precall.viewmodel.PreCallViewModel
import com.kaleyra.video_sdk.call.ringing.model.RingingUiState
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
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
            .debounce(AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
            .onEach { amIWaitingOthers -> _uiState.update { it.copy(amIWaitingOthers = amIWaitingOthers) } }
            .takeWhile { !it }
            .launchIn(viewModelScope)
    }

    fun accept() {
        call.getValue()?.connect()
    }

    fun decline() {
        call.getValue()?.end()
    }

    companion object {

        const val AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS = 2000L
        
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RingingViewModel(configure) as T
                }
            }
    }

}
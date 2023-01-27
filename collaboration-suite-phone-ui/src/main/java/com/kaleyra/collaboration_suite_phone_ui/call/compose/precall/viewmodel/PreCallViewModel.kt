package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import kotlinx.coroutines.flow.*

internal class PreCallViewModel(configure: suspend () -> Configuration) : BaseViewModel<PreCallUiState>(configure) {
    override fun initialState() = PreCallUiState()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    init {
        val participants = call.flatMapLatest { it.participants }
        val recording = call.map { it.extras.recording }
        // TODO add watermark

        participants
            .map { it.me }
            .flatMapLatest { me ->
                me.streams.mapToStreamsUi(me.displayName, me.displayImage)
            }
            .onEach { streams -> _uiState.update { it.copy(stream = streams.firstOrNull()) } }
            .launchIn(viewModelScope)

        participants
            .toOtherDisplayNames()
            .onEach { parts -> _uiState.update { it.copy(participants = parts) } }
            .launchIn(viewModelScope)

        call
            .isGroupCall()
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        recording
            .mapToRecordingUi()
            .onEach { rec -> _uiState.update { it.copy(recording = rec) } }
            .launchIn(viewModelScope)
    }

    fun answer() {
        call.getValue()?.connect()
    }

    fun decline() {
        call.getValue()?.end()
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PreCallViewModel(configure) as T
            }
        }
    }
}

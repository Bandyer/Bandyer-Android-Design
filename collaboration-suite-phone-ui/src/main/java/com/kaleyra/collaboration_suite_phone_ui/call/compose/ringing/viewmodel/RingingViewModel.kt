package com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.toOtherDisplayNames
import kotlinx.coroutines.flow.*

internal class RingingViewModel(configure: suspend () -> Configuration) : BaseViewModel<RingingUiState>(configure) {
    override fun initialState() = RingingUiState()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    val participants = call.flatMapLatest { it.participants }

    val recording = call.map { it.extras.recording }

    init {
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
            .onEach { participants -> _uiState.update { it.copy(participants = participants) } }
            .launchIn(viewModelScope)

        participants
            .isGroupCall()
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        recording
            .mapToRecordingUi()
            .onEach { recording -> _uiState.update { it.copy(recording = recording) } }
            .launchIn(viewModelScope)
    }

    fun answer() {
        call.getValue()?.connect()
    }

    fun decline() {
        call.getValue()?.end()
    }
}
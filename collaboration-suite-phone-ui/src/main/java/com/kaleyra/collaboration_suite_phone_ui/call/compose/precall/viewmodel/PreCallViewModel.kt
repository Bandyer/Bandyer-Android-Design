package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import kotlinx.coroutines.flow.*

internal class PreCallViewModel(configure: suspend () -> Configuration) : BaseViewModel<PreCallUiState>(configure) {
    override fun initialState() = PreCallUiState()

    init {
        company
            .toWatermarkInfo()
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .toMyStreamsUi()
            .onEach { streams -> _uiState.update { it.copy(stream = streams.firstOrNull()) } }
            .launchIn(viewModelScope)

        call
            .toOtherDisplayNames()
            .onEach { parts -> _uiState.update { it.copy(participants = parts) } }
            .launchIn(viewModelScope)

        call
            .isGroupCall()
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        call
            .toRecordingTypeUi()
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

package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.UserMessageViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isVideoIncoming
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal abstract class PreCallViewModel<T : PreCallUiState<T>>(configure: suspend () -> Configuration) : BaseViewModel<T>(configure), UserMessageViewModel {

    abstract override fun initialState(): T

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        theme
            .toWatermarkInfo(companyName)
            .onEach { watermarkInfo -> _uiState.update { it.clone(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .isVideoIncoming()
            .onEach { isVideoIncoming -> _uiState.update { it.clone(isVideoIncoming = isVideoIncoming) } }
            .launchIn(viewModelScope)

        call
            .toMyStreamsUi()
            .onEach { streams -> _uiState.update { it.clone(video = streams.firstOrNull()?.video) } }
            .launchIn(viewModelScope)

        call
            .toOtherDisplayNames()
            .onEach { names ->
                if (uiState.value.participants.value == names) return@onEach
                _uiState.update { it.clone(participants = ImmutableList(names)) }
            }
            .launchIn(viewModelScope)

        call
            .toOtherDisplayImages()
            .onEach { images ->
                val avatar = images.firstOrNull()
                if (avatar == null || uiState.value.avatar?.value == avatar) return@onEach
                _uiState.update { it.clone(avatar = ImmutableUri(avatar)) }
            }
            .launchIn(viewModelScope)

        call
            .toInCallParticipants()
            .onEach { participants -> _uiState.update { it.clone(isConnecting = participants.size > 1) } }
            .launchIn(viewModelScope)
    }
}

package com.kaleyra.video_sdk.call.precall.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import com.kaleyra.video_sdk.call.mapper.InputMapper.isVideoIncoming
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.video_sdk.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.video_sdk.call.precall.model.PreCallUiState
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal abstract class PreCallViewModel<T : PreCallUiState<T>>(configure: suspend () -> Configuration) : BaseViewModel<T>(configure),
    UserMessageViewModel {

    abstract override fun initialState(): T

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        company
            .flatMapLatest { it.combinedTheme }
            .toWatermarkInfo(company.flatMapLatest { it.name })
            .onEach { watermarkInfo -> _uiState.update { it.clone(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _uiState.update { it.clone(isLink = call.first().isLink) }
        }

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

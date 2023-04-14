package com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Effect
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.toCurrentVirtualBackground
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class VirtualBackgroundViewModel(configure: suspend () -> Configuration) : BaseViewModel<VirtualBackgroundUiState>(configure) {

    override fun initialState() = VirtualBackgroundUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    init {
        call
            .toCurrentVirtualBackground()
            .onEach { background -> _uiState.update { it.copy(currentBackground = background) } }
            .launchIn(viewModelScope)

        call
            .toVirtualBackgroundsUi()
            .onEach { backgrounds ->
                _uiState.update { it.copy(backgrounds = ImmutableList(backgrounds)) }
            }
            .launchIn(viewModelScope)
    }

    fun setEffect(background: VirtualBackgroundUi) {
        val call = call.getValue()
        val me = call?.participants?.value?.me
        val stream = me?.streams?.value?.firstOrNull { it.id == CAMERA_STREAM_ID }
        val video = stream?.video?.value ?: return
        val image = call.effects.preselected.value.takeIf { it is Effect.Video.Background.Image } as? Effect.Video
        val blur = call.effects.available.value.firstOrNull { it is Effect.Video.Background.Blur } as? Effect.Video
        video.tryApplyEffect(
            when {
                background == VirtualBackgroundUi.Blur && blur != null -> blur
                background == VirtualBackgroundUi.Image && image != null -> image
                else -> Effect.Video.None
            }
        )
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VirtualBackgroundViewModel(configure) as T
                }
            }
    }
}
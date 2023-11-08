package com.kaleyra.video_sdk.call.virtualbackground.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Effect
import com.kaleyra.video_common_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toCurrentVirtualBackgroundUi
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class VirtualBackgroundViewModel(configure: suspend () -> Configuration) : BaseViewModel<VirtualBackgroundUiState>(configure) {

    override fun initialState() = VirtualBackgroundUiState()

    init {
        call
            .toCurrentVirtualBackgroundUi()
            .onEach { background -> _uiState.update { it.copy(currentBackground = background) } }
            .launchIn(viewModelScope)

        call
            .toVirtualBackgroundsUi()
            .onEach { backgrounds ->
                _uiState.update { it.copy(backgroundList = ImmutableList(backgrounds)) }
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
                background is VirtualBackgroundUi.Blur && blur != null -> blur
                background is VirtualBackgroundUi.Image && image != null -> image
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
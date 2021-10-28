package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

@Suppress("UNCHECKED_CAST")
internal object NavGraphViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        NavGraphViewModel(ProvidersHolder.callProvider!!) as T
}

internal class NavGraphViewModel(private val callLogicProvider: CallLogicProvider) : ViewModel() {

    val participants: Flow<CallParticipants> = callLogicProvider.call.flatMapConcat { call -> call.participants }

    private val cameraStream: Flow<Stream?> =
        participants
            .map { it.me }
            .flatMapConcat { it.streams }
            .map { streams ->
                streams.firstOrNull { it.video.firstOrNull { video -> video?.source is Input.Video.Source.Camera.Internal } != null }
            }

    var isCameraEnabled = false
    val cameraEnabled: Flow<Boolean?> = cameraStream.filter { it != null }
        .flatMapConcat { it!!.video }
        .flatMapConcat { it!!.enabled }
        .onEach { isCameraEnabled = it == true }

    var isMicEnabled = false
    val micEnabled: Flow<Boolean?> = cameraStream.filter { it != null }
        .flatMapConcat { it!!.audio }
        .flatMapConcat { it!!.enabled }
        .onEach { isMicEnabled = it == true }

    fun enableCamera(enable: Boolean) = callLogicProvider.enableCamera(enable)

    fun enableMic(enable: Boolean) = callLogicProvider.enableMic(enable)

    fun answer() = callLogicProvider.answer()

    fun hangUp() = callLogicProvider.hangup()
}
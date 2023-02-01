package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startMicrophone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CallViewModel(configure: suspend () -> Configuration) :
    BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val maxFeatured = MutableStateFlow(1)

    private val streams = call
        .flatMapLatest { it.participants }
        .reduceToStreamsUi()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    init {
        // TODO add watermark

//        call
//            .map { it.extras.preferredType }
//            .take(1)
//            .onEach {
//                if (!viewModel.micPermission.value.isAllowed && it.hasAudio() && it.isAudioEnabled()) viewModel.onRequestMicPermission(this)
//                if (!viewModel.camPermission.value.isAllowed && it.hasVideo() && it.isVideoEnabled()) viewModel.onRequestCameraPermission(this)
//            }
//            .launchIn(lifecycleScope)

        val myStreams = call
            .flatMapLatest { it.participants }
            .flatMapLatest { it.me.streams }

        val myStreamsIds = myStreams.map { streams -> streams.map { it.id } }

        var featuredStreams = listOf<StreamUi>()
        var thumbnailsStreams = listOf<StreamUi>()
        streams
           .onEach { streams ->
               val added = streams - featuredStreams.toSet() - thumbnailsStreams.toSet()
               val removedFeatured = featuredStreams - streams.toSet()
               val removedThumbnails = thumbnailsStreams - streams.toSet()
               val newFeatured = (featuredStreams + added - removedFeatured.toSet()).take(maxFeatured.value)
               val newThumbnails = thumbnailsStreams + added - newFeatured.toSet() - removedThumbnails.toSet()
               featuredStreams = newFeatured
               thumbnailsStreams = newThumbnails
               _uiState.update {
                   it.copy(featuredStreams = ImmutableList(newFeatured), thumbnailStreams = ImmutableList(newThumbnails))
               }
           }
           .launchIn(viewModelScope)

        call
            .toCallStateUi()
            .onEach { callState ->
                _uiState.update { it.copy(callState = callState) }
            }
            .launchIn(viewModelScope)

        call
            .isGroupCall()
            .onEach { isGroupCall ->
                _uiState.update { it.copy(isGroupCall = isGroupCall) }
            }
            .launchIn(viewModelScope)

        call
            .isRecording()
            .onEach { isRecording ->
                _uiState.update { it.copy(isRecording = isRecording) }
            }
            .launchIn(viewModelScope)
    }

    fun startMicrophone(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.startMicrophone(context)
        }
    }

    fun startCamera(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.startCamera(context)
        }
    }

    fun setNumberOfFeaturedStreams(number: Int) {
        maxFeatured.value = number
    }

    fun moveThumbnailToFeatured() {

    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}
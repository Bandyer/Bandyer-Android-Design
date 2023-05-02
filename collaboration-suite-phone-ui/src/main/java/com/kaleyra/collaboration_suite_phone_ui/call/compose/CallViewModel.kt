package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startMicrophone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.isRecording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.toStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val streams = call
        .debounce(300)
        .toStreamsUi()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    private val maxNumberOfFeaturedStreams = MutableStateFlow(DEFAULT_FEATURED_STREAMS_COUNT)

    private val streamsHandler = StreamsHandler(
        streams = streams,
        nOfMaxFeatured = maxNumberOfFeaturedStreams,
        coroutineScope = viewModelScope
    )

    private var fullscreenStreamId = MutableStateFlow<String?>(null)

    init {
        // TODO add watermark

        streamsHandler.streamsArrangement
            .onEach { (featuredStreams, thumbnailsStreams) ->
                _uiState.update {
                    it.copy(
                        featuredStreams = ImmutableList(featuredStreams),
                        thumbnailStreams = ImmutableList(thumbnailsStreams)
                    )
                }
            }
            .launchIn(viewModelScope)

        combine(
            streamsHandler.streamsArrangement.map { it.first },
            fullscreenStreamId
        ) { featuredStreams, fullscreenStreamId ->
            val stream = featuredStreams.find { it.id == fullscreenStreamId }
            _uiState.update { it.copy(fullscreenStream = stream) }
        }.launchIn(viewModelScope)

        call
            .hasVideo()
            .onEach { hasVideo ->
                _uiState.update { it.copy(isAudioOnly = !hasVideo) }
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

    fun updateStreamsArrangement(isMediumSizeDevice: Boolean) {
        val count = when {
            !isMediumSizeDevice -> 2
            else -> 4
        }
        maxNumberOfFeaturedStreams.value = count
    }

    fun swapThumbnail(stream: StreamUi) = streamsHandler.swapThumbnail(stream)

    fun fullscreenStream(streamId: String?) {
        fullscreenStreamId.value = streamId
    }

    companion object {

        const val DEFAULT_FEATURED_STREAMS_COUNT = 2

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}
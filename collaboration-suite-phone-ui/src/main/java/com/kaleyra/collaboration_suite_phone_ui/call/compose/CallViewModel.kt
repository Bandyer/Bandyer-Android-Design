package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.util.Rational
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startMicrophone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.toMyCameraStream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.toStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

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

    private var onCallEnded: (suspend () -> Unit)? = null

    private var onPipAspectRatio: ((Rational) -> Unit)? = null

    init {
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

        company
            .toWatermarkInfo()
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .hasVideo()
            .onEach { hasVideo -> _uiState.update { it.copy(isAudioOnly = !hasVideo) } }
            .launchIn(viewModelScope)

        call
            .toCallStateUi()
            .onEach { callState ->
                _uiState.update { it.copy(callState = callState) }
                if (callState !is CallStateUi.Disconnected.Ended) return@onEach
                onCallEnded?.invoke()
            }
            .launchIn(viewModelScope)

        call
            .isGroupCall()
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        call
            .toRecordingUi()
            .onEach { rec -> _uiState.update { it.copy(recording = rec) } }
            .launchIn(viewModelScope)

        callUserMessageProvider
            .recordingUserMessage()
            .onEach { message ->
                _uiState.update {
                    it.copy(userMessages = it.userMessages.copy(recordingMessage = message))
                }
            }.launchIn(viewModelScope)

        callUserMessageProvider
            .mutedUserMessage()
            .onEach { message ->
                _uiState.update {
                    it.copy(userMessages = it.userMessages.copy(mutedMessage = message))
                }
            }.launchIn(viewModelScope)

        call
            .map { it.withFeedback }
            .combine(call.isConnected()) { withFeedback, isConnected ->
                val showFeedback = withFeedback && isConnected
                _uiState.update { it.copy(showFeedback = showFeedback) }
                isConnected
            }
            .takeWhile { !it }
            .launchIn(viewModelScope)

        uiState
            .toPipAspectRatio()
            .onEach { onPipAspectRatio?.invoke(it) }
            .launchIn(viewModelScope)
    }

    fun startMicrophone(context: FragmentActivity) {
        viewModelScope.launch {
            val call = call.getValue()
            if (call?.toMyCameraStream()?.audio?.value != null) return@launch
            call?.startMicrophone(context)
        }
    }

    fun startCamera(context: FragmentActivity) {
        viewModelScope.launch {
            val call = call.getValue()
            if (call?.toMyCameraStream()?.video?.value != null) return@launch
            call?.startCamera(context)
        }
    }

    fun hangUp() {
        call.getValue()?.end()
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

    fun sendUserFeedback(rating: Float, comment: String) {
        val call = call.getValue() ?: return
        val me = call.participants.value.me
        me.feedback.value = CallParticipant.Me.Feedback(rating.toInt(), comment)
    }

    fun setOnCallEnded(block: suspend () -> Unit) {
        onCallEnded = block
    }

    fun setOnPipAspectRatio(block: (Rational) -> Unit) {
        onPipAspectRatio = block
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
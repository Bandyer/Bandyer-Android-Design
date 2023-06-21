package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.util.Rational
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.toMyCameraStream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isAudioOnly
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isAudioVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.amIAlone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.toStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

    private val streams = call
        .toStreamsUi()
        .debounce {
            if (it.size == 1) SINGLE_STREAM_DEBOUNCE_MILLIS
            else STREAMS_DEBOUNCE_MILLIS
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    private val callState = call
        .toCallStateUi()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    private val maxNumberOfFeaturedStreams = MutableStateFlow(DEFAULT_FEATURED_STREAMS_COUNT)

    private val streamsHandler = StreamsHandler(
        streams = streams.map { streams -> streams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID } },
        nOfMaxFeatured = maxNumberOfFeaturedStreams,
        coroutineScope = viewModelScope
    )

    private var fullscreenStreamId = MutableStateFlow<String?>(null)

    private var onCallEnded: (suspend () -> Unit)? = null

    private var onPipAspectRatio: ((Rational) -> Unit)? = null

    init {
        streamsHandler.streamsArrangement
            .onEach { (featuredStreams, thumbnailsStreams) ->
                val feat = featuredStreams
                val thumb = thumbnailsStreams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID }
                _uiState.update {
                    it.copy(
                        featuredStreams = ImmutableList(feat),
                        thumbnailStreams = ImmutableList(thumb)
                    )
                }
            }
            .launchIn(viewModelScope)

        combine(streams, fullscreenStreamId) { streams, fullscreenStreamId ->
            val stream = streams.find { it.id == fullscreenStreamId }
            _uiState.update { it.copy(fullscreenStream = stream) }
        }.launchIn(viewModelScope)

        theme
            .toWatermarkInfo(companyName)
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .isAudioOnly()
            .onEach { isAudioOnly -> _uiState.update { it.copy(isAudioOnly = isAudioOnly) } }
            .launchIn(viewModelScope)

        callState
            .onEach { callState ->
                _uiState.update { it.copy(callState = callState) }
                when (callState) {
                    is CallStateUi.Disconnected.Ended -> onCallEnded?.invoke()
                    is CallStateUi.Reconnecting -> fullscreenStream(null)
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)

        combine(
            callState,
            call.isAudioVideo(),
            streams.hasAtLeastAVideoEnabled()
        ) { callState, isAudioVideo, hasAtLeastAVideoEnabled ->
            val enable = callState == CallStateUi.Connected && (isAudioVideo || hasAtLeastAVideoEnabled)
            _uiState.update { it.copy(doAVideoHasBeenEnabled = enable) }
            enable
        }.takeWhile { !it }.launchIn(viewModelScope)

        call
            .isGroupCall()
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        call.amIAlone()
            .debounce { if (it) 5000L else 0L }
            .onEach { amIAlone -> _uiState.update { it.copy(amIAlone = amIAlone) } }
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
            call?.inputs?.request(context, Inputs.Type.Microphone)
        }
    }

    fun startCamera(context: FragmentActivity) {
        viewModelScope.launch {
            val call = call.getValue()
            if (call?.toMyCameraStream()?.video?.value != null) return@launch
            call?.inputs?.request(context, Inputs.Type.Camera.Internal)
            call?.inputs?.request(context, Inputs.Type.Camera.External)
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

    fun swapThumbnail(streamId: String) = streamsHandler.swapThumbnail(streamId)

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
        const val STREAMS_DEBOUNCE_MILLIS = 300L
        const val SINGLE_STREAM_DEBOUNCE_MILLIS = 5000L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}
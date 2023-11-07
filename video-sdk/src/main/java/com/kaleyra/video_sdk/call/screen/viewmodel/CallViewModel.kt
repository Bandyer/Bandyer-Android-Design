package com.kaleyra.video_sdk.call.screen.viewmodel

import android.util.Rational
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.*
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.DisplayModeEvent
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIAlone
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.isConnected
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioVideo
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.video_sdk.call.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.arrangement.StreamsHandler
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.utils.CallExtensions.toMyCameraStream
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure), UserMessageViewModel {

    override fun initialState() = CallUiState()

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    val theme = company
        .flatMapLatest { it.combinedTheme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CompanyUI.Theme())

    private val streams: Flow<List<StreamUi>> =
        combine(call.toInCallParticipants(), call.toStreamsUi()) { participants, streams -> participants to streams }
        .debounce { (participants: List<CallParticipant>, streams: List<StreamUi>) ->
            if (participants.size != 1 && streams.size == 1) SINGLE_STREAM_DEBOUNCE_MILLIS
            else 0L
        }
        .map { (_: List<CallParticipant>, streams: List<StreamUi>) -> streams }
        .shareInEagerly(viewModelScope)

    private val callState = call
        .toCallStateUi()
        .shareInEagerly(viewModelScope)

    private val maxNumberOfFeaturedStreams = MutableStateFlow(DEFAULT_FEATURED_STREAMS_COUNT)

    private val streamsHandler = StreamsHandler(
        streams = streams.map { streams -> streams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID } },
        nOfMaxFeatured = maxNumberOfFeaturedStreams,
        coroutineScope = viewModelScope
    )

    private var fullscreenStreamId = MutableStateFlow<String?>(null)

    private var onCallEnded: MutableSharedFlow<(suspend (Boolean, Boolean, Boolean) -> Unit)> = MutableSharedFlow(replay = 1)

    private var onPipAspectRatio:  MutableSharedFlow<(Rational) -> Unit> = MutableSharedFlow(replay = 1)

    private var onDisplayMode: MutableSharedFlow<(CallUI.DisplayMode) -> Unit> = MutableSharedFlow(replay = 1)

    private var onAudioOrVideoChanged: MutableSharedFlow<(Boolean, Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    init {
        viewModelScope.launch {
            val result = withTimeoutOrNull(NULL_CALL_TIMEOUT) {
                call.firstOrNull()
            }
            result ?: onCallEnded.first().invoke(false, false, false)
        }

        CallUserMessagesProvider.start(call)

        streamsHandler.streamsArrangement
            .combine(callState) { (featuredStreams, thumbnailsStreams), state ->
                val thumbnails = thumbnailsStreams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID }
                _uiState.update {
                    it.copy(
                        featuredStreams = ImmutableList(featuredStreams),
                        thumbnailStreams = ImmutableList(thumbnails)
                    )
                }
            }
            .launchIn(viewModelScope)

        combine(streams, fullscreenStreamId) { streams, fullscreenStreamId ->
            val stream = streams.find { it.id == fullscreenStreamId }
            _uiState.update { it.copy(fullscreenStream = stream) }
        }.launchIn(viewModelScope)

        company
            .flatMapLatest { it.combinedTheme }
            .toWatermarkInfo(company.flatMapLatest { it.name })
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .isAudioOnly()
            .onEach { isAudioOnly -> _uiState.update { it.copy(isAudioOnly = isAudioOnly) } }
            .launchIn(viewModelScope)

        callState
            .filter { it is CallStateUi.Disconnecting || it is CallStateUi.Disconnected.Ended }
            .combine(onCallEnded) { callState, onCallEnded ->
                onCallEnded.invoke(
                    uiState.value.showFeedback,
                    callState is CallStateUi.Disconnected.Ended.Error,
                    callState is CallStateUi.Disconnected.Ended.Kicked
                )
            }
            .launchIn(viewModelScope)

        callState
            .filter { it is CallStateUi.Reconnecting }
            .onEach { fullscreenStream(null) }
            .launchIn(viewModelScope)

        callState
            .onEach { callState -> _uiState.update { it.copy(callState = callState) } }
            .launchIn(viewModelScope)

        combine(
            callState,
            call.isAudioVideo(),
            streams.hasAtLeastAVideoEnabled()
        ) { callState, isAudioVideo, hasAtLeastAVideoEnabled ->
            val enable = callState == CallStateUi.Connected && (isAudioVideo || hasAtLeastAVideoEnabled)
            _uiState.update { it.copy(shouldAutoHideSheet = enable) }
            enable
        }.takeWhile { !it }.launchIn(viewModelScope)

        call
            .isGroupCall(company.flatMapLatest { it.id })
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

        combine(
            call.flatMapLatest { it.displayModeEvent },
            onDisplayMode
        ) { event, onDisplayMode ->
                if (lastDisplayModeEvent?.id == event.id) return@combine
                lastDisplayModeEvent = event
                onDisplayMode.invoke(event.displayMode)
            }
            .combine(callState) { _, callState -> callState}
            .takeWhile { it !is CallStateUi.Disconnected.Ended  }
            .launchIn(viewModelScope)

        call
            .map { it.withFeedback }
            .combine(call.isConnected()) { withFeedback, isConnected ->
                val showFeedback = withFeedback && isConnected
                _uiState.update { it.copy(showFeedback = showFeedback) }
                isConnected
            }
            .takeWhile { !it }
            .launchIn(viewModelScope)

        combine(
            uiState.toPipAspectRatio(),
            onPipAspectRatio
        ) { aspectRatio, onPipAspectRatio ->
            onPipAspectRatio.invoke(aspectRatio)
        }.launchIn(viewModelScope)

        combine(
            call.flatMapLatest { it.preferredType },
            onAudioOrVideoChanged
        ) { preferredType, onAudioOrVideoChanged ->
            onAudioOrVideoChanged.invoke(
                preferredType.isAudioEnabled(),
                preferredType.isVideoEnabled()
            )
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        CallUserMessagesProvider.dispose()
    }

    fun startMicrophone(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.audio?.value != null) return
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Microphone) }
    }

    fun startCamera(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.video?.value != null) return
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Camera.Internal) }
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Camera.External) }
    }

    fun hangUp() {
        call.getValue()?.end()
    }

    fun updateStreamsArrangement(isMediumSizeDevice: Boolean) {
        val count = when {
            !isMediumSizeDevice -> 2
            else                -> 4
        }
        maxNumberOfFeaturedStreams.value = count
    }

    fun swapThumbnail(streamId: String) = streamsHandler.swapThumbnail(streamId)

    fun fullscreenStream(streamId: String?) {
        fullscreenStreamId.value = streamId
    }

    fun sendUserFeedback(rating: Float, comment: String) {
        val call = call.getValue() ?: return
        val me = call.participants.value.me ?: return
        me.feedback.value = CallParticipant.Me.Feedback(rating.toInt(), comment)
    }

    fun setOnCallEnded(block: suspend (hasFeedback: Boolean, hasErrorOccurred: Boolean, hasBeenKicked: Boolean) -> Unit) {
        viewModelScope.launch {
            onCallEnded.emit(block)
        }
    }

    fun setOnPipAspectRatio(block: (Rational) -> Unit) {
        viewModelScope.launch {
            onPipAspectRatio.emit(block)
        }
    }

    fun setOnDisplayMode(block: (CallUI.DisplayMode) -> Unit) {
        viewModelScope.launch {
            onDisplayMode.emit(block)
        }
    }

    fun setOnAudioOrVideoChanged(block: (isAudioEnabled: Boolean, isVideoEnabled: Boolean) -> Unit) {
        viewModelScope.launch {
            onAudioOrVideoChanged.emit(block)
        }
    }

    companion object {

        private var lastDisplayModeEvent: DisplayModeEvent? = null

        const val DEFAULT_FEATURED_STREAMS_COUNT = 2
        const val SINGLE_STREAM_DEBOUNCE_MILLIS = 5000L
        const val NULL_CALL_TIMEOUT = 1000L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}
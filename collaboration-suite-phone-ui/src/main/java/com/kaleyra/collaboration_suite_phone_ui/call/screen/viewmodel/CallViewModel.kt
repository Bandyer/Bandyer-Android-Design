package com.kaleyra.collaboration_suite_phone_ui.call.screen.viewmodel

import android.util.Rational
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.conference.*
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.collaboration_suite_phone_ui.call.utils.CallExtensions.toMyCameraStream
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.stream.arrangement.StreamsHandler
import com.kaleyra.collaboration_suite_phone_ui.call.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.common.viewmodel.UserMessageViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.CallStateMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.InputMapper.isAudioVideo
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.amIAlone
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure),
    UserMessageViewModel {

    override fun initialState() = CallUiState()

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    val theme: Flow<CompanyUI.Theme>
        get() = company.flatMapLatest { it.combinedTheme }

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

    private var onCallEnded: (suspend (Boolean, Boolean, Boolean) -> Unit)? = null

    private var onPipAspectRatio: ((Rational) -> Unit)? = null

    private var onAudioOrVideoChanged: MutableSharedFlow<(Boolean, Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    init {
        viewModelScope.launch {
            val result = withTimeoutOrNull(NULL_CALL_TIMEOUT) {
                call.firstOrNull()
            }
            result ?: onCallEnded?.invoke(false, false, false)
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
            .onEach { callState ->
                _uiState.update { it.copy(callState = callState) }
                when (callState) {
                    is CallStateUi.Disconnected.Ended -> onCallEnded?.invoke(
                        uiState.value.showFeedback,
                        callState is CallStateUi.Disconnected.Ended.Error,
                        callState is CallStateUi.Disconnected.Ended.Kicked
                    )

                    is CallStateUi.Reconnecting -> fullscreenStream(null)
                    else                              -> Unit
                }
            }
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

        combine(
            call.flatMapLatest { it.preferredType },
            onAudioOrVideoChanged
        ) { preferredType, onAudioOrVideoChanged ->
            onAudioOrVideoChanged.invoke(preferredType.isAudioEnabled(), preferredType.isVideoEnabled())
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        CallUserMessagesProvider.dispose()
        call.getValue()?.inputs?.releaseAll()
    }

    fun startMicrophone(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.audio?.value != null) return
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Microphone) }
    }

    fun startCamera(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.video?.value != null) return
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Camera.Internal).getOrNull<Input.Video>() }
        viewModelScope.launch { call.inputs.request(context, Inputs.Type.Camera.External).getOrNull<Input.Video>() }
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
        val me = call.participants.value.me
        me.feedback.value = CallParticipant.Me.Feedback(rating.toInt(), comment)
    }

    fun setOnCallEnded(block: suspend (hasFeedback: Boolean, hasErrorOccurred: Boolean, hasBeenKicked: Boolean) -> Unit) {
        onCallEnded = block
    }

    fun setOnPipAspectRatio(block: (Rational) -> Unit) {
        onPipAspectRatio = block
    }

    fun setOnAudioOrVideoChanged(block: (isAudioEnabled: Boolean, isVideoEnabled: Boolean) -> Unit) {
        viewModelScope.launch {
            onAudioOrVideoChanged.emit(block)
        }
    }

    companion object {

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
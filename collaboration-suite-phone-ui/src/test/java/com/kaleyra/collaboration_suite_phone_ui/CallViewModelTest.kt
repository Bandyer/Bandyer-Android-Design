package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration.Success
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.Theme
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamsHandler
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CallViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()
    
    private val callMock = mockk<CallUI>(relaxed = true)

    private val inputsMock = mockk<Inputs>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val viewMock = mockk<VideoStreamView>(relaxed = true)

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val streamMock1 = mockk<Stream>(relaxed = true)

    private val streamMock2 = mockk<Stream>(relaxed = true)

    private val streamMock3 = mockk<Stream>()

    private val streamMock4 = mockk<Stream>()

    private val myStreamMock = mockk<Stream.Mutable>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val companyNameMock = "Kaleyra"

    private val themeMock = mockk<Theme>()

    private val dayLogo = mockk<Uri>()

    private val nightLogo = mockk<Uri>()

    @Before
    fun setUp() {
        mockkConstructor(StreamsHandler::class)
        every { anyConstructed<StreamsHandler>().swapThumbnail(any()) } returns Unit
        mockkConstructor(CallUserMessagesProvider::class)
        every { anyConstructed<CallUserMessagesProvider>().recordingUserMessage() } returns MutableStateFlow(RecordingMessage.Started())
        every { anyConstructed<CallUserMessagesProvider>().mutedUserMessage() } returns MutableStateFlow(MutedMessage(null))
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { inputs } returns inputsMock
            every { participants } returns MutableStateFlow(callParticipantsMock)
            every { extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
            every { extras.recording.type } returns Call.Recording.Type.OnConnect
            every { extras.preferredType } returns Call.PreferredType()
            every { state } returns MutableStateFlow<Call.State>(Call.State.Disconnected)
        }
        with(callParticipantsMock) {
            every { others } returns listOf(participantMock1, participantMock2)
            every { me } returns participantMeMock
            every { list } returns others + me
            every { creator() } returns me
        }
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock3) {
            every { id } returns "streamId3"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock4) {
            every { id } returns "streamId4"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(myStreamMock) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
            every { displayName } returns MutableStateFlow("myDisplayName")
            every { displayImage } returns MutableStateFlow(uriMock)
            every { feedback } returns MutableStateFlow(null)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { displayName } returns MutableStateFlow("displayName1")
            every { displayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { displayName } returns MutableStateFlow("displayName2")
            every { displayImage } returns MutableStateFlow(uriMock)
        }
        with(themeMock) {
            every { day } returns mockk {
                every { logo } returns dayLogo
            }
            every { night } returns mockk {
                every { logo } returns nightLogo
            }
        }
        viewModel = spyk(CallViewModel { Success(phoneBoxMock, mockk(), MutableStateFlow(companyNameMock), MutableStateFlow(themeMock), mockk()) })
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testCallUiState_callStateUpdated() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val current = viewModel.uiState.first().callState
        assertEquals(CallStateUi.Disconnected, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().callState
        assertEquals(CallStateUi.Connected, new)
    }

    @Test
    fun testCallUiState_featuredStreamsUpdated() = runTest {
        val current = viewModel.uiState.first().featuredStreams
        assertEquals(ImmutableList<StreamUi>(listOf()), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().featuredStreams
        val featuredStreamsIds = new.value.map { it.id }
        assertEquals(listOf(streamMock1.id, streamMock2.id), featuredStreamsIds)
    }

    @Test
    fun testCallUiState_thumbnailStreamsUpdated() = runTest {
        val current = viewModel.uiState.first().thumbnailStreams
        assertEquals(ImmutableList<StreamUi>(listOf()), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().thumbnailStreams
        val thumbnailStreamsIds = new.value.map { it.id }
        assertEquals(listOf(streamMock3.id, myStreamMock.id), thumbnailStreamsIds)
    }

    @Test
    fun testCallUiState_streamsDoNotContainsMyScreenShare() = runTest {
        with(callParticipantsMock) {
            every { others } returns listOf()
            every { me } returns participantMeMock
            every { list } returns others + me
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { myStreamMock.id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
        advanceUntilIdle()
        val featured = viewModel.uiState.first().featuredStreams
        val thumbnails = viewModel.uiState.first().thumbnailStreams
        val streams = featured.value + thumbnails.value
        assertEquals(listOf<String>(), streams.map { it.id })
    }

    @Test
    fun testCallUiState_isGroupCallUpdated() = runTest {
        val current = viewModel.uiState.first().isGroupCall
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isGroupCall
        assertEquals(true, new)
    }

    @Test
    fun testCallUiState_isRecordingUpdated() = runTest {
        val current = viewModel.uiState.first().recording?.state
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().recording?.state
        assertEquals(RecordingStateUi.Started, new)
    }

    @Test
    fun testCallUiState_recordingTypeUpdated() = runTest {
        val current = viewModel.uiState.first().recording?.type
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().recording?.type
        assertEquals(RecordingTypeUi.OnConnect, new)
    }

    @Test
    fun testCallUiState_isAudioOnlyUpdated() = runTest {
        every { callMock.extras.preferredType } returns Call.PreferredType(video = null)
        val current = viewModel.uiState.first().isAudioOnly
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isAudioOnly
        assertEquals(true, new)
    }

    @Test
    fun testRecordingUserMessageReceived_userMessagesUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().userMessages.recordingMessage
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun testCallUiState_watermarkInfoUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().watermarkInfo
        assertEquals(WatermarkInfo(text = "Kaleyra", logo = Logo(dayLogo, nightLogo)), actual)
    }

    @Test
    fun testMutedUserMessageReceived_userMessagesUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().userMessages.mutedMessage
        Assert.assertNotEquals(null, actual)
    }

    @Test
    fun testCallUiState_showFeedbackUpdated() = runTest {
        with(callMock) {
            every { withFeedback } returns true
            every { state } returns MutableStateFlow(Call.State.Connected)
        }
        advanceUntilIdle()
        val actual = viewModel.uiState.first().showFeedback
        assertEquals(true, actual)
    }

    @Test
    fun testStartMicrophone() = runTest {
        val audioMock = mockk<Input.Audio>(relaxed = true)
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
            every { video } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(audioMock)

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify { inputsMock.request(contextMock, Inputs.Type.Microphone) }
    }

    @Test
    fun testStartMicrophone_audioIsAlreadyInitialized_audioIsNotStarted() = runTest {
        val audioMock = mockk<Input.Audio>(relaxed = true)
        val newAudioMock = mockk<Input.Audio>(relaxed = true)
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
            every { video } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(newAudioMock)

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Microphone) }
    }

    @Test
    fun testStartCamera() = runTest {
        val cameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
            every { enabled } returns MutableStateFlow(true)
            every { view } returns MutableStateFlow(null)
        }
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(null)
            every { audio } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(cameraMock)

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
    }

    @Test
    fun testStartCamera_videoIsAlreadyInitialized_cameraIsNotStarted() = runTest {
        val cameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
            every { view } returns MutableStateFlow(null)
            every { enabled } returns MutableStateFlow(true)
        }
        val newCameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
            every { view } returns MutableStateFlow(null)
            every { enabled } returns MutableStateFlow(true)
        }
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(cameraMock)
            every { audio } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(newCameraMock)

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        assertEquals(cameraMock, myStreamMock.video.value)
    }

    @Test
    fun testFullscreenStream() = runTest {
        viewModel.fullscreenStream(streamMock1.id)
        advanceUntilIdle()
        val fullscreenStream = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1.id, fullscreenStream?.id)
    }

    @Test
    fun testUpdateStreamArrangement_isNotMediumSizeDevice_twoMaxFeaturedStreams() = runTest {
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock4))
        viewModel.updateStreamsArrangement(false)
        advanceUntilIdle()
        val actual = viewModel.uiState.first().featuredStreams
        assertEquals(2, actual.count())
    }

    @Test
    fun testUpdateStreamArrangement_isMediumSizeDevice_fourMaxFeaturedStreams() = runTest {
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock4))
        viewModel.updateStreamsArrangement(true)
        advanceUntilIdle()
        val actual = viewModel.uiState.first().featuredStreams
        assertEquals(4, actual.count())
    }

    @Test
    fun testSwapThumbnail() {
        viewModel.swapThumbnail("streamId")
        verify { anyConstructed<StreamsHandler>().swapThumbnail("streamId") }
    }

    @Test
    fun testHangUp() = runTest {
        advanceUntilIdle()
        viewModel.hangUp()
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testSendUserFeedback() = runTest {
        advanceUntilIdle()
        viewModel.sendUserFeedback(3f, "comment")
        val actual = participantMeMock.feedback.first()
        val expected = CallParticipant.Me.Feedback(3, "comment")
        assertEquals(expected, actual)
    }

    @Test
    fun fullscreenStreamRemovedFromStreams_fullscreenStreamIsNull() = runTest {
        val participantStreams = MutableStateFlow(listOf(streamMock1, streamMock2))
        every { participantMock1.streams } returns participantStreams
        viewModel.fullscreenStream(streamMock1.id)
        advanceUntilIdle()
        val actual = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1.id, actual?.id)

        participantStreams.value = listOf(streamMock2)
        advanceUntilIdle()
        val new = viewModel.uiState.first().fullscreenStream
        assertEquals(null, new?.id)
    }

    @Test
    fun `fullscreen stream is set to null on reconnecting call state`() = runTest {
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { callMock.state } returns callState
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        viewModel.fullscreenStream(streamMock1.id)
        advanceUntilIdle()
        val actual = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1.id, actual?.id)

        callState.value = Call.State.Reconnecting
        advanceUntilIdle()
        val new = viewModel.uiState.first().fullscreenStream
        assertEquals(null, new?.id)
    }
}

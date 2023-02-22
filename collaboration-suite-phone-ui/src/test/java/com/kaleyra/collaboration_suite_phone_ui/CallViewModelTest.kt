package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallViewModel
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
    
    private val callMock = mockk<CallUI>()

    private val inputsMock = mockk<Inputs>()

    private val uriMock = mockk<Uri>()

    private val viewMock = mockk<VideoStreamView>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true) {
        every { id } returns "videoId"
        every { this@mockk.view } returns MutableStateFlow(viewMock)
        every { enabled } returns MutableStateFlow(true)
    }

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
        every { id } returns "myVideoId"
        every { this@mockk.view } returns MutableStateFlow(viewMock)
        every { enabled } returns MutableStateFlow(true)
    }

    private val streamMock1 = mockk<Stream> {
        every { id } returns "streamId1"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    private val streamMock2 = mockk<Stream> {
        every { id } returns "streamId2"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    private val streamMock3 = mockk<Stream> {
        every { id } returns "streamId3"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    private val myStreamMock = mockk<Stream.Mutable> {
        every { id } returns "myStreamId"
        every { this@mockk.video } returns MutableStateFlow(myVideoMock)
    }

    private val callParticipantsMock = mockk<CallParticipants>()

    private val participantMeMock = mockk<CallParticipant.Me> {
        every { userId } returns "userId1"
        every { streams } returns MutableStateFlow(listOf(myStreamMock))
        every { displayName } returns MutableStateFlow("myDisplayName")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    private val participantMock1 = mockk<CallParticipant> {
        every { userId } returns "userId1"
        every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        every { displayName } returns MutableStateFlow("displayName1")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    private val participantMock2 = mockk<CallParticipant> {
        every { userId } returns "userId2"
        every { streams } returns MutableStateFlow(listOf(streamMock3))
        every { displayName } returns MutableStateFlow("displayName2")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    @Before
    fun setUp() {
        viewModel = spyk(CallViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk()) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.inputs } returns inputsMock
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { callParticipantsMock.me } returns participantMeMock
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
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
    fun testCallUiState_isGroupCallUpdated() = runTest {
        val current = viewModel.uiState.first().isGroupCall
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isGroupCall
        assertEquals(true, new)
    }

    @Test
    fun testCallUiState_isRecordingUpdated() = runTest {
        val current = viewModel.uiState.first().isRecording
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isRecording
        assertEquals(true, new)
    }

    @Test
    fun testStartMicrophone() = runTest {
        val audioMock = mockk<Input.Audio>()
        val myStreamMock = mockk<Stream.Mutable> {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(audioMock)

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify { inputsMock.request(contextMock, Inputs.Type.Microphone) }
        assertEquals(audioMock, myStreamMock.audio.value)
    }

    @Test
    fun testStartCamera() = runTest {
        val cameraMock = mockk<Input.Video.Camera.Internal>()
        val myStreamMock = mockk<Stream.Mutable> {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(null)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(cameraMock)

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        assertEquals(cameraMock, myStreamMock.video.value)
    }
}

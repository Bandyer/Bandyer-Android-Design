package com.kaleyra.collaboration_suite_phone_ui

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

class CallViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallViewModel

    private val inputsMock = mockk<Inputs>()

    @Before
    fun setUp() {
        viewModel = spyk(CallViewModel { Configuration.Success(Mocks.phoneBoxMock, Mocks.chatBoxMock, Mocks.usersDescriptionMock) })
        every { Mocks.phoneBoxMock.call } returns MutableStateFlow(Mocks.callMock)
        every { Mocks.callMock.inputs } returns inputsMock
        every { Mocks.callMock.participants } returns MutableStateFlow(PhoneBoxMocks.callParticipantsMock)
        every { PhoneBoxMocks.callParticipantsMock.others } returns listOf(PhoneBoxMocks.participantMock1, PhoneBoxMocks.participantMock2)
        every { PhoneBoxMocks.callParticipantsMock.me } returns PhoneBoxMocks.participantMeMock
        every { Mocks.callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testCallUiState_callStateUpdated() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(Call.State.Connected)
        val current = viewModel.uiState.first().callState
        assertEquals(CallState.Disconnected, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().callState
        assertEquals(CallState.Connected, new)
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
            every { id } returns CallStreamDelegate.MY_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        every { PhoneBoxMocks.participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
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
            every { id } returns CallStreamDelegate.MY_STREAM_ID
            every { video } returns MutableStateFlow(null)
        }
        every { PhoneBoxMocks.participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val contextMock = mockk<FragmentActivity>()
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(cameraMock)

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        assertEquals(cameraMock, myStreamMock.video.value)
    }
}

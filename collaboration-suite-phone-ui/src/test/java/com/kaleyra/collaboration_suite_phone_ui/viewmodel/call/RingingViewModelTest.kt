package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel.RingingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RingingViewModelTest : PreCallViewModelTest<RingingViewModel, RingingUiState>() {

    private val recordingMock = mockk<Call.Recording>()

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = spyk(RingingViewModel {
            Configuration.Success(
                conferenceMock,
                mockk(),
                companyMock
            )
        })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun testPreCallUiState_recordingUpdated() = runTest {
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val current = viewModel.uiState.first().recording
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().recording
        val expected = RecordingTypeUi.OnConnect
        assertEquals(expected, new)
    }

    @Test
    fun testPreCallUiState_answeredUpdated() = runTest {
        with(callMock) {
            every { state } returns MutableStateFlow(Call.State.Connecting)
            every { participants } returns MutableStateFlow(callParticipantsMock)
        }
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { myStreamMock.state } returns MutableStateFlow(Stream.State.Live)
        every { streamMock1.id } returns "streamId"
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { others } returns listOf(participantMock1)
            every { creator() } returns mockk()
        }
        val current = viewModel.uiState.first().answered
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().answered
        assertEquals(true, new)
    }

    @Test
    fun testPreCallUiState_amIWaitingForOthersUpdated() = runTest {
        with(callMock) {
            every { state } returns MutableStateFlow(Call.State.Connected)
            every { participants } returns MutableStateFlow(callParticipantsMock)
        }
        with(participantMock1) {
            every { streams } returns MutableStateFlow(listOf())
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(participantMeMock) {
            every { streams } returns MutableStateFlow(listOf())
            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
        }
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { others } returns listOf(participantMock1)
            every { creator() } returns mockk()
        }
        val current = viewModel.uiState.first().amIWaitingOthers
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().amIWaitingOthers
        assertEquals(true, new)
    }

    @Test
    fun testCallAnswer() = runTest {
        advanceUntilIdle()
        viewModel.accept()
        verify(exactly = 1) { callMock.connect() }
    }

    @Test
    fun testCallDecline() = runTest {
        advanceUntilIdle()
        viewModel.decline()
        verify(exactly = 1) { callMock.end() }
    }
}
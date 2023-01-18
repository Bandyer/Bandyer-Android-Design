package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatBoxMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.usersDescriptionMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.callParticipantsMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.myVideoMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMeMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMock1
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMock2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.phoneBoxMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.recordingMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.uriMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel.PreCallViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PreCallViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PreCallViewModel

    @Before
    fun setUp() {
        viewModel = spyk(PreCallViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, usersDescriptionMock) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { callParticipantsMock.me } returns participantMeMock
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        every { callMock.extras.recording } returns recordingMock
    }

    @Test
    fun testRingingUiState_streamUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().stream
        val expected = StreamUi(
            id = "myStreamId",
            video = VideoUi(
                id = myVideoMock.id,
                view = myVideoMock.view.value,
                isEnabled = myVideoMock.enabled.value
            ),
            username = "myDisplayName",
            avatar = ImmutableUri(uriMock)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testRingingUiState_participantsUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().participants
        val expected = listOf("displayName1", "displayName2")
        assertEquals(expected, actual)
    }

    @Test
    fun testRingingUiState_isGroupCallUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().isGroupCall
        assertEquals(true, actual)
    }

    @Test
    fun testRingingUiState_recordingUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().recording
        val expected = Recording.OnConnect
        assertEquals(expected, actual)
    }

    @Test
    fun testCallAnswer() = runTest {
        advanceUntilIdle()
        viewModel.answer()
        verify { callMock.connect() }
    }

    @Test
    fun testCallDecline() = runTest {
        advanceUntilIdle()
        viewModel.decline()
        verify { callMock.end() }
    }
}
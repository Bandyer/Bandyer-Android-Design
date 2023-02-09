package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.RecordingUi
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

@OptIn(ExperimentalCoroutinesApi::class)
class PreCallViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PreCallViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

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

    private val recordingMock = mockk<Call.Recording>()

    @Before
    fun setUp() {
        viewModel = spyk(PreCallViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk()) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { callParticipantsMock.me } returns participantMeMock
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        every { callMock.extras.recording } returns recordingMock
    }

    @Test
    fun testPreCallUiState_streamUpdated() = runTest {
        val current = viewModel.uiState.first().stream
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().stream
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
        assertEquals(expected, new)
    }

    @Test
    fun testPreCallUiState_participantsUpdated() = runTest {
        val current = viewModel.uiState.first().participants
        assertEquals(listOf<String>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().participants
        val expected = listOf("displayName1", "displayName2")
        assertEquals(expected, new)
    }

    @Test
    fun testPreCallUiState_isGroupCallUpdated() = runTest {
        val current = viewModel.uiState.first().isGroupCall
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isGroupCall
        assertEquals(true, new)
    }

    @Test
    fun testPreCallUiState_recordingUpdated() = runTest {
        val current = viewModel.uiState.first().recording
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().recording
        val expected = RecordingUi.OnConnect
        assertEquals(expected, new)
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
package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.Theme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel.PreCallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreCallViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private data class PreCallUiStateImpl(
        override val video: VideoUi? = null,
        override val avatar: ImmutableUri? = null,
        override val participants: ImmutableList<String> = ImmutableList(listOf()),
        override val watermarkInfo: WatermarkInfo? = null,
        override val isLink: Boolean = false,
        override val isConnecting: Boolean = false,
        override val userMessages: UserMessages = UserMessages()
    ) : PreCallUiState<PreCallUiStateImpl> {
        override fun clone(
            video: VideoUi?,
            avatar: ImmutableUri?,
            participants: ImmutableList<String>,
            watermarkInfo: WatermarkInfo?,
            isLink: Boolean,
            isConnecting: Boolean,
            userMessages: UserMessages
        ): PreCallUiStateImpl {
            return copy(
                video = video,
                avatar = avatar,
                participants = participants,
                watermarkInfo = watermarkInfo,
                isLink = isLink,
                isConnecting = isConnecting,
                userMessages = userMessages
            )
        }

    }

    private class PreCallViewModelImpl(configure: suspend () -> Configuration): PreCallViewModel<PreCallUiStateImpl>(configure) {
        override fun initialState() = PreCallUiStateImpl()

    }

    private lateinit var viewModel: PreCallViewModel<PreCallUiStateImpl>

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock1 = mockk<Uri>()

    private val uriMock2 = mockk<Uri>()

    private val uriMock3 = mockk<Uri>()

    private val viewMock = mockk<VideoStreamView>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    private val myStreamMock = mockk<Stream.Mutable>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val participantMeMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val recordingMock = mockk<Call.Recording>()

    private val companyNameMock = "Kaleyra"

    private val themeMock = mockk<Theme>()

    private val dayLogo = mockk<Uri>()

    private val nightLogo = mockk<Uri>()

    @Before
    fun setUp() {
        viewModel = spyk(PreCallViewModelImpl { Configuration.Success(phoneBoxMock, mockk(), MutableStateFlow(companyNameMock), MutableStateFlow(themeMock), mockk()) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        with(callMock) {
            every { participants } returns MutableStateFlow(callParticipantsMock)
            every { extras.recording } returns recordingMock
        }
        with(callParticipantsMock) {
            every { others } returns listOf(participantMock1, participantMock2)
            every { me } returns participantMeMock
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
        with(myStreamMock) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserID"
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
            every { displayName } returns MutableStateFlow("myDisplayName")
            every { displayImage } returns MutableStateFlow(uriMock1)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { displayName } returns MutableStateFlow("displayName1")
            every { displayImage } returns MutableStateFlow(uriMock2)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { displayName } returns MutableStateFlow("displayName2")
            every { displayImage } returns MutableStateFlow(uriMock3)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(themeMock) {
            every { day } returns mockk {
                every { logo } returns dayLogo
            }
            every { night } returns mockk {
                every { logo } returns nightLogo
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPreCallUiState_streamUpdated() = runTest {
        val current = viewModel.uiState.first().video
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().video
        val expected = VideoUi(
                id = myVideoMock.id,
                view = myVideoMock.view.value?.let { ImmutableView(it) },
                isEnabled = myVideoMock.enabled.value
            )
        assertEquals(expected, new)
    }

    @Test
    fun testPreCallUiState_participantsUpdated() = runTest {
        val current = viewModel.uiState.first().participants
        assertEquals(ImmutableList(listOf<String>()), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().participants
        val expected = ImmutableList(listOf("displayName1", "displayName2"))
        assertEquals(expected, new)
    }

//    @Test
//    fun testPreCallUiState_recordingUpdated() = runTest {
//        val current = viewModel.uiState.first().recording
//        assertEquals(null, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().recording
//        val expected = RecordingTypeUi.OnConnect
//        assertEquals(expected, new)
//    }

    @Test
    fun testPreCallUiState_isLinkUpdated() = runTest {
        every { callMock.isLink } returns true
        val current = viewModel.uiState.first().isConnecting
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isConnecting
        assertEquals(true, new)
    }

    @Test
    fun testPreCallUiState_isConnectingUpdated() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        every { participantMock1.state } returns MutableStateFlow(CallParticipant.State.InCall)
        val current = viewModel.uiState.first().isConnecting
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isConnecting
        assertEquals(true, new)
    }

    @Test
    fun testPreCallUiState_avatarUpdated() = runTest {
        val current = viewModel.uiState.first().avatar
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().avatar
        val expected = ImmutableUri(uriMock2)
        assertEquals(expected, new)
    }

    @Test
    fun testCallUiState_watermarkInfoUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().watermarkInfo
        assertEquals(WatermarkInfo(text = "Kaleyra", logo = Logo(dayLogo, nightLogo)), actual)
    }

//    @Test
//    fun testCallAnswer() = runTest {
//        advanceUntilIdle()
//        viewModel.answer()
//        verify(exactly = 1) { callMock.connect() }
//    }
//
//    @Test
//    fun testCallDecline() = runTest {
//        advanceUntilIdle()
//        viewModel.decline()
//        verify(exactly = 1) { callMock.end() }
//    }
}
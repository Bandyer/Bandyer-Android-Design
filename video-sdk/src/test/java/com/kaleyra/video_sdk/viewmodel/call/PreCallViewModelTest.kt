package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import com.kaleyra.video.Company
import com.kaleyra.video.conference.*
import com.kaleyra.video.conference.Call.PreferredType
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.theme.CompanyThemeManager
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.precall.model.PreCallUiState
import com.kaleyra.video_sdk.call.precall.viewmodel.PreCallViewModel
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class PreCallViewModelTest<VM: PreCallViewModel<T>, T: PreCallUiState<T>> {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    protected lateinit var viewModel: VM

    protected val conferenceMock = mockk<ConferenceUI>()

    protected val companyMock = mockk<Company>()

    protected val callMock = mockk<CallUI>(relaxed = true)

    private val themeMock = mockk<CompanyUI.Theme>()

    private val preferredTypeMock =  MutableStateFlow(PreferredType.audioVideo())

    private val uriMock1 = mockk<Uri>()

    private val uriMock2 = mockk<Uri>()

    private val uriMock3 = mockk<Uri>()

    private val viewMock = mockk<VideoStreamView>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    protected val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    protected val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    protected val callParticipantsMock = mockk<CallParticipants>()

    protected val participantMeMock = mockk<CallParticipant.Me>(relaxed = true)

    protected val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val dayLogo = mockk<Uri>()

    private val nightLogo = mockk<Uri>()

    open fun setUp() {
        mockkObject(CallUserMessagesProvider)
        mockkObject(ContactDetailsManager)
        mockkObject(CompanyThemeManager)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callParticipantsMock) {
            every { others } returns listOf(participantMock1, participantMock2)
            every { me } returns participantMeMock
            every { creator() } returns mockk()
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
            every { state } returns MutableStateFlow(Stream.State.Live)
        }
        with(participantMeMock) {
            every { userId } returns "myUserID"
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock1)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock2)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock3)
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        }
        every { callMock.preferredType } returns preferredTypeMock
        every { companyMock.name } returns MutableStateFlow("Kaleyra")
        with(themeMock) {
            every { day } returns mockk {
                every { logo } returns dayLogo
            }
            every { night } returns mockk {
                every { logo } returns nightLogo
            }
        }
        every { companyMock.combinedTheme } returns flowOf(themeMock)
    }

    open fun tearDown() {
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

    @Test
    fun testPreCallUiState_isLinkUpdated() = runTest {
        every { callMock.isLink } returns true
        val current = viewModel.uiState.first().isLink
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isLink
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

    @Test
    fun testPreCallUiState_isVideoIncomingUpdated() = runTest {
        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        val current = viewModel.uiState.first().isVideoIncoming
        assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isVideoIncoming
        assertEquals(true, new)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }

}
package com.kaleyra.video_common_ui.mapper

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIAlone
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_common_ui.mapper.StreamMapper.doAnyOfMyStreamsIsLive
import com.kaleyra.video_common_ui.mapper.StreamMapper.doOthersHaveStreams
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val viewMock = mockk<VideoStreamView>()

    private val uriMock = mockk<Uri>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    private val myStreamMock1 = mockk<Stream.Mutable>()

    private val myStreamMock2 = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val callParticipantsMock = mockk<CallParticipants>()

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        // only needed for toCallStateUi function
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1, participantMock2)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
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
        with(myStreamMock1) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myStreamMock2) {
            every { id } returns "myStreamId2"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun noStreamIsLive_doAnyOfMyStreamsIsLive_false() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Open)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Closed)
        val callFlow = flowOf(callMock)
        val actual = callFlow.doAnyOfMyStreamsIsLive().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun oneStreamIsLive_doAnyOfMyStreamsIsLive_true() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)
        val callFlow = flowOf(callMock)
        val actual = callFlow.doAnyOfMyStreamsIsLive().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun newLiveStreamAdded_doAnyOfMyStreamsIsLive_true() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)

        val myStreams = MutableStateFlow(listOf(myStreamMock1))
        every { participantMeMock.streams } returns myStreams
        val result = flowOf(callMock).doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)

        myStreams.value = listOf(myStreamMock1, myStreamMock2)
        val new = result.first()
        Assert.assertEquals(true, new)
    }

    @Test
    fun newLiveStreamRemoved_doAnyOfMyStreamsIsLive_false() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)

        val myStreams = MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
        every { participantMeMock.streams } returns myStreams
        val result = flowOf(callMock).doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(true, actual)

        myStreams.value = listOf(myStreamMock1)
        val new = result.first()
        Assert.assertEquals(false, new)
    }

    @Test
    fun doNotHaveStreams_doAnyOfMyStreamsIsLive_false() = runTest {
        every { participantMeMock.streams } returns MutableStateFlow(listOf())
        val result = flowOf(callMock).doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun myStreamGoesLive_doAnyOfMyStreamsIsLive_true() = runTest {
        val state = MutableStateFlow<Stream.State>(Stream.State.Closed)
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns state

        val result = flowOf(callMock).doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)

        state.value = Stream.State.Live
        val new = result.first()
        Assert.assertEquals(true, new)
    }

    @Test
    fun myStreamNoMoreLive_doAnyOfMyStreamsIsLive_false() = runTest {
        val state = MutableStateFlow<Stream.State>(Stream.State.Live)
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns state

        val result = flowOf(callMock).doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(true, actual)

        state.value = Stream.State.Closed
        val new = result.first()
        Assert.assertEquals(false, new)
    }

    @Test
    fun noParticipants_doOthersHaveStreams_false() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val callFlow = flowOf(callMock)
        val actual = callFlow.doOthersHaveStreams().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun otherHaveNoStreams_doOthersHaveStreams_false() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { participantMock1.streams } returns MutableStateFlow(listOf())
        every { participantMock2.streams } returns MutableStateFlow(listOf())
        val callFlow = flowOf(callMock)
        val actual = callFlow.doOthersHaveStreams().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun othersHaveStreams_doOthersHaveStreams_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf())
        val callFlow = flowOf(callMock)
        val actual = callFlow.doOthersHaveStreams().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `I have live streams and other participants have streams, am i alone is false`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callFlow.doOthersHaveStreams() } returns flowOf(true)
            every { callFlow.doAnyOfMyStreamsIsLive() } returns flowOf(true)
        }
        val result = callFlow.amIAlone()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun `I have no live stream, am I alone is true`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callFlow.doOthersHaveStreams() } returns flowOf(true)
            every { callFlow.doAnyOfMyStreamsIsLive() } returns flowOf(false)
        }
        val result = callFlow.amIAlone()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `other participants does not have streams, am I alone is true`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callFlow.doOthersHaveStreams() } returns flowOf(false)
            every { callFlow.doAnyOfMyStreamsIsLive() } returns flowOf(true)
        }
        val result = callFlow.amIAlone()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `call is connected and I am alone and there is only one in call participant, am I waiting others is true`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        mockkObject(ParticipantMapper)
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        with(ParticipantMapper) {
            every { callFlow.toInCallParticipants() } returns flowOf(listOf(participantMeMock))
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callFlow.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `call is not connected, am I waiting others is false`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        mockkObject(ParticipantMapper)
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        with(ParticipantMapper) {
            every { callFlow.toInCallParticipants() } returns flowOf(listOf(participantMeMock))
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val result = callFlow.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun `I am no alone, am I waiting others is false`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        mockkObject(ParticipantMapper)
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(false)
        }
        with(ParticipantMapper) {
            every { callFlow.toInCallParticipants() } returns flowOf(listOf(participantMeMock))
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callFlow.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun `there are more than one in call participants, am I waiting others is false`() = runTest {
        val callFlow = flowOf(callMock)
        mockkObject(StreamMapper)
        mockkObject(ParticipantMapper)
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        with(ParticipantMapper) {
            every { callFlow.toInCallParticipants() } returns flowOf(listOf(participantMeMock, participantMock1))
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callFlow.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }
}
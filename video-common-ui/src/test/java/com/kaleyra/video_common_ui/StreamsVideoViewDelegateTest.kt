package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.*
import com.kaleyra.video_common_ui.call.StreamsVideoViewDelegate
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StreamsVideoViewDelegateTest {

    private val streamsVideoViewDelegate = object : StreamsVideoViewDelegate { }

    private val context = RuntimeEnvironment.getApplication()

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantMock = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val myStreamMock = mockk<Stream.Mutable>()

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val videoMock1 = mockk<Input.Video>(relaxed = true)

    private val videoMock2 = mockk<Input.Video>(relaxed = true)

    @Before
    fun setUp() {
        every { participantsMock.list } returns listOf(meMock, participantMock)
        with(meMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
        }
        with(participantMock) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1))
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock2))
        }
        with(myStreamMock) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock1)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock2)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(null)
        }
        with(videoMock1) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(null)
        }
        with(videoMock2) {
            every { id } returns "videoId2"
            every { view } returns MutableStateFlow(null)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun setStreamVideoView_viewsAreSet() = runTest(UnconfinedTestDispatcher()) {
        streamsVideoViewDelegate.setStreamsVideoView(context, MutableStateFlow(participantsMock), backgroundScope)
        assertNotEquals(null, myVideoMock.view.value)
        assertNotEquals(null, videoMock1.view.value)
    }

    @Test
    fun setStreamsVideoViewWhenAParticipantVideoIsNull() = runTest(UnconfinedTestDispatcher()) {
        every { myStreamMock.video } returns MutableStateFlow(null)
        streamsVideoViewDelegate.setStreamsVideoView(context, MutableStateFlow(participantsMock), backgroundScope)
        assertNotEquals(null, videoMock1.view.value)
    }

    @Test
    fun setStreamsVideoView_viewSetOnNewParticipant() = runTest(UnconfinedTestDispatcher()) {
        val participants = MutableStateFlow(participantsMock)
        streamsVideoViewDelegate.setStreamsVideoView(context, participants, backgroundScope)
        assertNotEquals(null, myVideoMock.view.value)
        assertNotEquals(null, videoMock1.view.value)

        val newParticipantsMock = mockk<CallParticipants>()
        every { newParticipantsMock.list } returns listOf(meMock, participantMock, participantMock2)
        participants.value = newParticipantsMock
        assertNotEquals(null, videoMock2.view.value)
    }

    @Test
    fun setStreamsVideoView_viewSetOnNewStream() = runTest(UnconfinedTestDispatcher()) {
        val streams = MutableStateFlow(listOf(streamMock1))
        every { participantMock.streams } returns streams
        streamsVideoViewDelegate.setStreamsVideoView(context, MutableStateFlow(participantsMock), backgroundScope)
        assertNotEquals(null, myVideoMock.view.value)
        assertNotEquals(null, videoMock1.view.value)

        streams.value = listOf(streamMock1, streamMock2)
        assertNotEquals(null, videoMock2.view.value)
    }

    @Test
    fun setStreamsVideoView_viewSetOnNewVideo() = runTest(UnconfinedTestDispatcher()) {
        val video = MutableStateFlow(videoMock1)
        every { streamMock1.video } returns video
        streamsVideoViewDelegate.setStreamsVideoView(context, MutableStateFlow(participantsMock), backgroundScope)
        assertNotEquals(null, myVideoMock.view.value)
        assertNotEquals(null, videoMock1.view.value)

        video.value = videoMock2
        assertNotEquals(null, videoMock2.view.value)
    }
}
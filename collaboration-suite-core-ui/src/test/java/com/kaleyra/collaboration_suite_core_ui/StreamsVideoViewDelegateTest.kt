package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.call.StreamsVideoViewDelegate
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class StreamsVideoViewDelegateTest {

    private val streamsVideoViewDelegate = object : StreamsVideoViewDelegate { }

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantMock = mockk<CallParticipant>(relaxed = true)

    private val streamMock = mockk<Stream>(relaxed = true)

    private val videoMock = mockk<Input.Video>(relaxed = true)

    // TODO check to test this

//    @Test
//    fun setStreamsVideoView_viewSetOnOtherParticipantStreamVideo() = runTest(UnconfinedTestDispatcher()) {
//        every { participantsMock.list } returns listOf(participantMock)
//        every { participantMock.streams } returns MutableStateFlow(listOf(streamMock))
//        every { streamMock.video } returns MutableStateFlow(videoMock)
//        every { videoMock.view } returns MutableStateFlow(null)
//        mockkConstructor(VideoStreamView::class)
////        every { anyConstructed<ViewGroup>().findViewById<View>(any()) } returns mockk()
//        streamsVideoViewDelegate.setStreamsVideoView(mockk(relaxed = true), flowOf(participantsMock), this)
//        assertNotEquals(null, videoMock.view)
//        stopCollecting()
//    }
//
//    @Test
//    fun setStreamsVideoView_viewSetOnMyStreamVideo() = runTest(UnconfinedTestDispatcher()) {
//
//    }
//
//    @Test
//    fun setStreamsVideoView_viewSetOnNewStream() = runTest(UnconfinedTestDispatcher()) {
//
//    }
//
//    @Test
//    fun setStreamsVideoView_viewSetOnNewVideo() = runTest(UnconfinedTestDispatcher()) {
//
//    }
}
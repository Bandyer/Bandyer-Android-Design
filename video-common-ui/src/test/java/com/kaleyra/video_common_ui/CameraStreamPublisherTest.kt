package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraStreamPublisherTest {

    private val cameraStreamPublisher = object : CameraStreamPublisher { }

    private val callMock = mockk<Call>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true) {
        every { me } returns meMock
    }

    @Before
    fun setUp() {
        every { meMock.streams } returns MutableStateFlow(listOf())
    }

    @Test
    fun meIsNull_addCameraStream_waitForMeToAddStreamIsCalled() = runTest {
        val nullMeParticipantsMock = mockk<CallParticipants>(relaxed = true) {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(nullMeParticipantsMock)
        every { callMock.participants } returns participantsFlow

        cameraStreamPublisher.addCameraStream(callMock, this)
        runCurrent()

        verify(exactly = 0) { meMock.addStream(CameraStreamPublisher.CAMERA_STREAM_ID) }

        participantsFlow.value = participantsMock
        runCurrent()

        verify(exactly = 1) { meMock.addStream(CameraStreamPublisher.CAMERA_STREAM_ID) }
    }

    @Test
    fun streamNotExists_addCameraStream_addStreamIsCalled() = runTest {
        every { callMock.participants } returns MutableStateFlow(participantsMock)

        cameraStreamPublisher.addCameraStream(callMock, this)
        runCurrent()

        verify(exactly = 1) { meMock.addStream(CameraStreamPublisher.CAMERA_STREAM_ID) }
    }

    @Test
    fun streamAlreadyExists_addCameraStream_addStreamIsNotPerformed() = runTest {
        val streamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
        }
        every { meMock.streams } returns MutableStateFlow(listOf(streamMock))
        every { callMock.participants } returns MutableStateFlow(participantsMock)

        cameraStreamPublisher.addCameraStream(callMock, this)
        runCurrent()

        verify(exactly = 0) { meMock.addStream(CameraStreamPublisher.CAMERA_STREAM_ID) }
    }
}
package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.TestHelper.stopCollecting
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamInputsDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraStreamInputsDelegateTest {

    private val cameraStreamInputsDelegate = object : CameraStreamInputsDelegate { }

    private val callMock = mockk<Call>()

    private val meMock = mockk<CallParticipant.Me>()

    @Before
    fun setUp() {
    }

    @Test
    fun streamUpdatedOnMyCameraVideoInput() = runTest(UnconfinedTestDispatcher()) {
        val myStream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
            every { video } returns MutableStateFlow(null)
        }
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        val participantsMock = mockk<CallParticipants>() {
            every { me } returns meMock
        }
        every { meMock.streams } returns MutableStateFlow(listOf(myStream))
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { extras.preferredType.hasVideo() } returns true
            every { inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        }
        cameraStreamInputsDelegate.updateCameraStreamOnInputs(callMock, this)
        assertEquals(videoMock,  meMock.streams.value.first().video.value)
        stopCollecting()
    }

    @Test
    fun setHDQualityOnCameraVideoStream() = runTest(UnconfinedTestDispatcher()) {
        val myStream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
            every { video } returns MutableStateFlow(null)
        }
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
        }
        val participantsMock = mockk<CallParticipants>() {
            every { me } returns meMock
        }
        every { meMock.streams } returns MutableStateFlow(listOf(myStream))
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { extras.preferredType.hasVideo() } returns true
            every { inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        }
        cameraStreamInputsDelegate.updateCameraStreamOnInputs(callMock, this)
        verify { videoMock.setQuality(Input.Video.Quality.Definition.HD) }
        stopCollecting()
    }

    @Test
    fun streamUpdatedOnAudioInput() {

    }

    @Test
    fun streamNotUpdatedOnCameraVideoInputWhenCallIsAudioOnly() {

    }

    @Test
    fun streamNotUpdatedOnApplicationVideoInput() {

    }

    @Test
    fun streamNotUpdatedOnScreenVideoInput() {

    }

    @Test
    fun streamNotUpdatedOnCustomVideoInput() {

    }

}
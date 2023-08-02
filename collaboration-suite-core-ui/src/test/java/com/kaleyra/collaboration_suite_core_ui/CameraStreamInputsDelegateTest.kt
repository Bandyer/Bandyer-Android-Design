package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call.PreferredType
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamInputsDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraStreamInputsDelegateTest {

    private val cameraStreamInputsDelegate = object : CameraStreamInputsDelegate {}

    private val callMock = mockk<Call>()

    private val participantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>()

    private val myStream = mockk<Stream.Mutable>(relaxed = true)


    @Before
    fun setUp() {
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { preferredType } returns MutableStateFlow(PreferredType.audioVideo())
        }
        with(participantsMock) {
            every { me } returns meMock
        }
        every { meMock.streams } returns MutableStateFlow(listOf(myStream))
        with(myStream) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
            every { video } returns MutableStateFlow(null)
        }
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnMyCameraVideoInput() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        cameraStreamInputsDelegate.handleCameraStreamVideo(callMock, backgroundScope)
        val actual = meMock.streams.value.first().video.value
        assertEquals(videoMock, actual)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnPreferredTypeChanged() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        val preferredType = MutableStateFlow(PreferredType.audioOnly())
        every { callMock.preferredType } returns preferredType
        cameraStreamInputsDelegate.handleCameraStreamVideo(callMock, backgroundScope)
        val actual = meMock.streams.value.first().video.value
        assertEquals(null, actual)
        preferredType.value = PreferredType.audioVideo()
        val new = meMock.streams.value.first().video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_setHDQualityOnCameraVideoStream() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        cameraStreamInputsDelegate.handleCameraStreamVideo(callMock, backgroundScope)
        verify { videoMock.setQuality(Input.Video.Quality.Definition.HD, any()) }
    }

    @Test
    fun handleCameraStreamAudio_streamUpdatedOnAudioInput() = runTest(UnconfinedTestDispatcher()) {
        val audioMock = mockk<Input.Audio>(relaxed = true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(audioMock))
        cameraStreamInputsDelegate.handleCameraStreamAudio(callMock, backgroundScope)
        val actual = meMock.streams.value.first().audio.value
        assertEquals(audioMock, actual)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnCameraVideoInputWhenCallIsAudioOnly() {
        every { callMock.preferredType } returns MutableStateFlow(PreferredType.audioOnly())
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnApplicationVideoInput() {
        val videoMock = mockk<Input.Video.Application>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnScreenVideoInput() {
        val videoMock = mockk<Input.Video.Screen>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnCustomVideoInput() {
        val videoMock = mockk<Input.Video.Custom>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    private fun checkStreamNotUpdatedOnVideoInput(videoMock: Input.Video) = runTest(UnconfinedTestDispatcher()) {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        cameraStreamInputsDelegate.handleCameraStreamVideo(callMock, backgroundScope)
        val actual = meMock.streams.value.first().video.value
        assertEquals(null, actual)
    }

}
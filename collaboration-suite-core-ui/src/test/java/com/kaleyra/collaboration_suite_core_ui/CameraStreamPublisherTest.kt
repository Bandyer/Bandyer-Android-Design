package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class CameraStreamPublisherTest {

    private val cameraStreamPublisher = object : CameraStreamPublisher { }

    @Test
    fun addCameraStream_addStreamIsCalled() {
        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
            every { streams } returns MutableStateFlow(listOf())
        }
        val participantsMock = mockk<CallParticipants>(relaxed = true) {
            every { me } returns meMock
        }
        val callMock = mockk<Call>(relaxed = true) {
            every { participants } returns MutableStateFlow(participantsMock)
        }
        cameraStreamPublisher.addCameraStream(callMock)
        verify { meMock.addStream(CameraStreamPublisher.CAMERA_STREAM_ID) }
    }
}
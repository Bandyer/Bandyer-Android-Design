package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.toMyCameraStream
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Test

class CallExtensionsTest {

    @Test
    fun meHasCameraStreamId_toMyCameraStream_cameraStream() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.id } returns CameraStreamPublisher.CAMERA_STREAM_ID
        val actual = call.toMyCameraStream()
        Assert.assertEquals(myStream, actual)
    }

    @Test
    fun meHasNotCameraStreamId_toMyCameraStream_null() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.id } returns ""
        val actual = call.toMyCameraStream()
        Assert.assertEquals(null, actual)
    }
}
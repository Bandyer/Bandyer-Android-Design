package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.getMyInternalCamera
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraUsingFrontLens
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isOngoing
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isOutgoing
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class CallExtensionsTest {

    @Test
    fun callHasUsbInput_hasUsbInput_true() {
        val call = mockk<Call>()
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        assertEquals(true, call.hasUsbInput())
    }

    @Test
    fun callDoesNotHaveUsbInput_hasUsbInput_false() {
        val call = mockk<Call>()
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        assertEquals(false, call.hasUsbInput())
    }

    @Test
    fun myStreamHasInternalCameraEnabled_isMyCameraEnabled_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamHasInternalCameraDisabled_isMyCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamGenericVideoEnabled_isMyCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.My>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(false, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamHasInternalCameraWithFrontLens_isMyInternalCameraUsingFrontLens_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        val lens = mockk<Input.Video.Camera.Internal.Lens>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.currentLens } returns MutableStateFlow(lens)
        every { lens.isRear } returns false
        assertEquals(true, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun myStreamHasInternalCameraWithRearLens_isMyInternalCameraUsingFrontLens_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        val lens = mockk<Input.Video.Camera.Internal.Lens>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.currentLens } returns MutableStateFlow(lens)
        every { lens.isRear } returns true
        assertEquals(false, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun myStreamHasGenericVideo_isMyInternalCameraUsingFrontLens_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.My>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(false, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun screenShareStream_isMyScreenShareEnabled_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Screen.My>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(true, call.isMyScreenShareEnabled())
    }

    @Test
    fun applicationScreenShareStream_isMyScreenShareEnabled_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Application>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(true, call.isMyScreenShareEnabled())
    }

    @Test
    fun cameraStream_isMyScreenShareEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(false, call.isMyScreenShareEnabled())
    }

    @Test
    fun callIsNotConnected_isNotConnected_true() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(mockk())
        assertEquals(true, call.isNotConnected())
    }

    @Test
    fun callIsConnected_isNotConnected_false() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Connected)
        assertEquals(false, call.isNotConnected())
    }

    @Test
    fun callDisconnectedAndIAmCallNotCreator_isIncoming_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns mockk()
        every { callParticipants.creator() } returns mockk()
        assertEquals(true, call.isIncoming())
    }

    @Test
    fun callDisconnectedAndIAmCallCreator_isIncoming_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { callParticipants.creator() } returns me
        assertEquals(false, call.isIncoming())
    }

    @Test
    fun callConnecting_isIncoming_false() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Connecting)
        assertEquals(false, call.isIncoming())
    }

    @Test
    fun callConnectingAndIAmCallCreator_isOutgoing_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        every { call.state } returns MutableStateFlow(Call.State.Connecting)
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
        every { callParticipants.creator() } returns me
        assertEquals(true, call.isOutgoing())
    }

    @Test
    fun callConnectingAndIAmNotCallCreator_isOutgoing_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        every { call.state } returns MutableStateFlow(Call.State.Connecting)
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns mockk()
        every { callParticipants.creator() } returns mockk()
        assertEquals(false, call.isOutgoing())
    }

    @Test
    fun callDisconnected_isOutgoing_false() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        assertEquals(false, call.isOutgoing())
    }

    @Test
    fun callConnecting_isOngoing_true() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Connecting)
        assertEquals(true, call.isOngoing())
    }

    @Test
    fun callConnected_isOngoing_true() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Connected)
        assertEquals(true, call.isOngoing())
    }

    @Test
    fun callDoesNotHaveCreator_isOngoing_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.creator() } returns null
        assertEquals(true, call.isOngoing())
    }

    @Test
    fun callDisconnected_isOngoing_false() {
        val call = mockk<Call>()
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.participants } returns MutableStateFlow(mockk(relaxed = true))
        assertEquals(false, call.isOngoing())
    }

    @Test
    fun myCameraEnabled_hasUsersWithCameraEnabled_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val stream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(me)
        every { me.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun myCameraDisabled_hasUsersWithCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val stream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(me)
        every { me.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun otherUserCameraEnabled_hasUsersWithCameraEnabled_true() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Camera>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun otherUserCameraDisabled_hasUsersWithCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Camera>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun onlyNullVideo_hasUsersWithCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(null)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun onlyScreenShareVideo_hasUsersWithCameraEnabled_false() {
        val call = mockk<Call>()
        val callParticipants = mockk<CallParticipants>()
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Screen>()
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun testGetMyInternalCamera() {
        val call = mockk<Call>()
        val camera = mockk<Input.Video.Camera.Internal>()
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(camera))
        assertEquals(camera, call.getMyInternalCamera())
    }
}
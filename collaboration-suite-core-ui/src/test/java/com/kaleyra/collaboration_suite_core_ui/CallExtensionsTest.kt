package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import android.net.Uri
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.conference.CallParticipants
import com.kaleyra.collaboration_suite.conference.Effects
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite.conference.Inputs
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite.sharedfolder.SharedFolder
import com.kaleyra.collaboration_suite.whiteboard.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
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
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CallExtensionsTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val call = mockk<CallUI>(relaxed = true)

    private val callParticipants = mockk<CallParticipants>(relaxed = true)

    private val me = mockk<CallParticipant.Me>(relaxed = true)

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        mockkObject(AppLifecycle)
        every { ContextRetainer.context } returns context
        every { call.participants } returns MutableStateFlow(callParticipants)
        every { callParticipants.me } returns me
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun callHasUsbInput_hasUsbInput_true() {
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        assertEquals(true, call.hasUsbInput())
    }

    @Test
    fun callDoesNotHaveUsbInput_hasUsbInput_false() {
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        assertEquals(false, call.hasUsbInput())
    }

    @Test
    fun myStreamHasInternalCameraEnabled_isMyCameraEnabled_true() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamHasInternalCameraDisabled_isMyCameraEnabled_false() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamGenericVideoEnabled_isMyCameraEnabled_false() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.My>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(false, call.isMyInternalCameraEnabled())
    }

    @Test
    fun myStreamHasInternalCameraWithFrontLens_isMyInternalCameraUsingFrontLens_true() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        val lens = mockk<Input.Video.Camera.Internal.Lens>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.currentLens } returns MutableStateFlow(lens)
        every { lens.isRear } returns false
        assertEquals(true, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun myStreamHasInternalCameraWithRearLens_isMyInternalCameraUsingFrontLens_false() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        val lens = mockk<Input.Video.Camera.Internal.Lens>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        every { video.currentLens } returns MutableStateFlow(lens)
        every { lens.isRear } returns true
        assertEquals(false, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun myStreamHasGenericVideo_isMyInternalCameraUsingFrontLens_false() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.My>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(false, call.isMyInternalCameraUsingFrontLens())
    }

    @Test
    fun screenShareStream_isMyScreenShareEnabled_true() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Screen.My>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(true, call.isMyScreenShareEnabled())
    }

    @Test
    fun applicationScreenShareStream_isMyScreenShareEnabled_true() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Application>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(true, call.isMyScreenShareEnabled())
    }

    @Test
    fun cameraStream_isMyScreenShareEnabled_false() {
        val myStream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { me.streams } returns MutableStateFlow(listOf(myStream))
        every { myStream.video } returns MutableStateFlow(video)
        assertEquals(false, call.isMyScreenShareEnabled())
    }

    @Test
    fun callIsNotConnected_isNotConnected_true() {
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        assertEquals(true, call.isNotConnected())
    }

    @Test
    fun callIsConnected_isNotConnected_false() {
        every { call.state } returns MutableStateFlow(Call.State.Connected)
        assertEquals(false, call.isNotConnected())
    }

    @Test
    fun callDisconnectedAndIAmCallNotCreator_isIncoming_true() {
        every { callParticipants.creator() } returns mockk()
        assertEquals(true, isIncoming(Call.State.Disconnected, callParticipants))
    }

    @Test
    fun callDisconnectedAndIAmCallCreator_isIncoming_false() {
        every { callParticipants.creator() } returns me
        assertEquals(false, isIncoming(Call.State.Disconnected, callParticipants))
    }

    @Test
    fun callConnecting_isIncoming_false() {
        assertEquals(false, isIncoming(Call.State.Connecting, mockk()))
    }

    @Test
    fun callConnectingAndIAmCallCreator_isOutgoing_true() {
        every { callParticipants.creator() } returns me
        assertEquals(true, isOutgoing(Call.State.Connecting, callParticipants))
    }

    @Test
    fun callConnectingAndIAmNotCallCreator_isOutgoing_false() {
        every { callParticipants.creator() } returns mockk()
        assertEquals(false, isOutgoing(Call.State.Connecting, callParticipants))
    }

    @Test
    fun callDisconnected_isOutgoing_false() {
        assertEquals(false, isOutgoing(Call.State.Disconnected, mockk(relaxed = true)))
    }

    @Test
    fun callConnecting_isOngoing_true() {
        assertEquals(true, isOngoing(Call.State.Connecting, mockk(relaxed = true)))
    }

    @Test
    fun callConnected_isOngoing_true() {
        assertEquals(true, isOngoing(Call.State.Connected, mockk(relaxed = true)))
    }

    @Test
    fun callDoesNotHaveCreator_isOngoing_true() {
        every { callParticipants.creator() } returns null
        assertEquals(true, isOngoing(Call.State.Disconnected, callParticipants))
    }

    @Test
    fun callDisconnected_isOngoing_false() {
        assertEquals(false, isOngoing(Call.State.Disconnected, mockk(relaxed = true)))
    }

    @Test
    fun myCameraEnabled_hasUsersWithCameraEnabled_true() {
        val stream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { callParticipants.list } returns listOf(me)
        every { me.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun myCameraDisabled_hasUsersWithCameraEnabled_false() {
        val stream = mockk<Stream.Mutable>()
        val video = mockk<Input.Video.Camera.Internal>()
        every { callParticipants.list } returns listOf(me)
        every { me.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun otherUserCameraEnabled_hasUsersWithCameraEnabled_true() {
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Camera>()
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(true, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun otherUserCameraDisabled_hasUsersWithCameraEnabled_false() {
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Camera>()
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(false)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun onlyNullVideo_hasUsersWithCameraEnabled_false() {
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(null)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun onlyScreenShareVideo_hasUsersWithCameraEnabled_false() {
        val user = mockk<CallParticipant>()
        val stream = mockk<Stream>()
        val video = mockk<Input.Video.Screen>()
        every { callParticipants.list } returns listOf(user)
        every { user.streams } returns MutableStateFlow(listOf(stream))
        every { stream.video } returns MutableStateFlow(video)
        every { video.enabled } returns MutableStateFlow(true)
        assertEquals(false, call.hasUsersWithCameraEnabled())
    }

    @Test
    fun testGetMyInternalCamera() {
        val camera = mockk<Input.Video.Camera.Internal>()
        every { call.inputs.availableInputs } returns MutableStateFlow(setOf(camera))
        assertEquals(camera, call.getMyInternalCamera())
    }

    @Test
    fun callOutgoing_shouldShowAsActivity_true() {
        mockkObject(ContextExtensions)
        every { callParticipants.creator() } returns me
        every { call.state } returns MutableStateFlow(Call.State.Connecting)
        every { context.isDND() } returns true
        every { context.isSilent() } returns true
        every { ContextRetainer.context } returns context
        val result = call.shouldShowAsActivity()
        assertEquals(true, result)
    }

    @Test
    fun dndEnabled_shouldShowAsActivity_false() {
        mockkObject(ContextExtensions)
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { context.isDND() } returns true
        every { context.isSilent() } returns false
        val result = call.shouldShowAsActivity()
        assertEquals(false, result)
    }

    @Test
    fun silentEnabled_shouldShowAsActivity_false() {
        mockkObject(ContextExtensions)
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { context.isDND() } returns false
        every { context.isSilent() } returns true
        val result = call.shouldShowAsActivity()
        assertEquals(false, result)
    }

    @Test
    fun linkCall_shouldShowAsActivity_true() {
        mockkObject(ContextExtensions)
        every { call.isLink } returns true
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { context.isDND() } returns true
        every { context.isSilent() } returns true
        val result = call.shouldShowAsActivity()
        assertEquals(true, result)
    }

    @Test
    fun testShowOnAppResumed() = runTest {
        val isInForeground = MutableStateFlow(false)
        every { AppLifecycle.isInForeground } returns isInForeground
        every { call.show() } returns true
        call.showOnAppResumed(this)
        runCurrent()
        verify(exactly = 0) { call.show() }
        isInForeground.value = true
        runCurrent()
        verify(exactly = 1) { call.show() }
    }
}
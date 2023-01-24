package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCallActions
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

class CallActionsMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val videoMock = mockk<Input.Video.Camera.Internal>()

    private val audioMock = mockk<Input.Audio>()

    private val streamMock = mockk<Stream.Mutable> {
        every { this@mockk.video } returns MutableStateFlow(videoMock)
        every { this@mockk.audio } returns MutableStateFlow(audioMock)
    }

    @Before
    fun setUp() {
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
    }

    @After
    fun tearDown() {
    }

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(mockk())
        every { Mocks.callMock.actions } returns MutableStateFlow(emptySet())
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf<CallAction>()
        assertEquals(expected, actual)
    }

    @Test
    fun filledCallActions_toCallActions_mappedCallActions() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(mockk())
        every { Mocks.callMock.actions } returns MutableStateFlow(
            setOf(
                CallUI.Action.ToggleMicrophone,
                CallUI.Action.ToggleCamera,
                CallUI.Action.SwitchCamera,
                CallUI.Action.HangUp,
                CallUI.Action.OpenChat.Full,
                CallUI.Action.OpenWhiteboard.Full,
                CallUI.Action.Audio,
                CallUI.Action.FileShare,
                CallUI.Action.ScreenShare
            )
        )
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.Camera(),
            CallAction.SwitchCamera(),
            CallAction.HangUp(),
            CallAction.Chat(),
            CallAction.Whiteboard(isEnabled = false),
            CallAction.Audio(),
            CallAction.FileShare(isEnabled = false),
            CallAction.ScreenShare(isEnabled = false),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_toCallActions_fileShareIsEnabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.FileShare))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.FileShare(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_toCallActions_screenShareIsEnabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ScreenShare))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.ScreenShare(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_toCallActions_whiteboardIsEnabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenWhiteboard.Full))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.Whiteboard(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_toCallActions_fileShareIsDisabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(mockk())
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.FileShare))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.FileShare(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_toCallActions_screenShareIsDisabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(mockk())
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ScreenShare))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.ScreenShare(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_toCallActions_whiteboardIsDisabled() = runTest {
        every { Mocks.callMock.state } returns MutableStateFlow(mockk())
        every { Mocks.callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenWhiteboard.Full))
        val call = MutableStateFlow(Mocks.callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.Whiteboard(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun isCameraEnabled_true() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        val me = MutableStateFlow(participantMeMock)
        val result = me.isCameraEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun isCameraEnabled_false() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        val me = MutableStateFlow(participantMeMock)
        val result = me.isCameraEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun isAudioEnabled_true() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        val me = MutableStateFlow(participantMeMock)
        val result = me.isMicEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun isAudioEnabled_false() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        val me = MutableStateFlow(participantMeMock)
        val result = me.isMicEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }
}
package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasAudio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isAudioOnly
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.hasVirtualBackground
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CallActionsMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val callFlow = MutableStateFlow(callMock)

    @Before
    fun setUp() {
        mockkObject(VirtualBackgroundMapper)
        mockkObject(InputMapper)
        every { callFlow.hasVirtualBackground() } returns flowOf(false)
        every { callFlow.isAudioOnly() } returns flowOf(false)
        every { callFlow.hasAudio() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(emptySet())
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf<CallAction>()
        assertEquals(expected, actual)
    }

    @Test
    fun allCallActions_toCallActions_mappedCallActions() = runTest {
        every { callMock.actions } returns MutableStateFlow(
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
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.Camera(),
            CallAction.SwitchCamera(),
            CallAction.HangUp(),
            CallAction.Audio(),
            CallAction.Chat(),
            CallAction.FileShare(),
            CallAction.ScreenShare(),
            CallAction.Whiteboard()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun hasVirtualBackgroundTrue_toCallActions_actionsListHasVirtualBackground() = runTest {
        every { callMock.actions } returns MutableStateFlow(emptySet())
        every { callFlow.hasVirtualBackground() } returns flowOf(true)
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.VirtualBackground())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleCameraActionAndCallHasVideo_toCallActions_actionsListHasCamera() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { callFlow.isAudioOnly() } returns flowOf(false)
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.Camera())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleCameraActionAndCallHasNoVideo_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { callFlow.isAudioOnly() } returns flowOf(true)
        val result = callFlow.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallAction>(), actual)
    }


    @Test
    fun switchCameraActionAndCallHasVideo_toCallActions_actionsListHasSwitchCamera() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { callFlow.isAudioOnly() } returns flowOf(false)
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.SwitchCamera())
        assertEquals(expected, actual)
    }

    @Test
    fun switchCameraActionAndCallHasNoVideo_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { callFlow.isAudioOnly() } returns flowOf(true)
        val result = callFlow.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallAction>(), actual)
    }

    @Test
    fun toggleMicActionAndCallHasAudio_toCallActions_actionsListHasMicrophone() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { callFlow.hasAudio() } returns flowOf(true)
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(CallAction.Microphone())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleMicActionAndCallHasNoAudio_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { callFlow.hasAudio() } returns flowOf(false)
        val result = callFlow.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallAction>(), actual)
    }

    @Test
    fun moreThan3Actions_toCallActions_hangUpIsIn4thPosition() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone, CallUI.Action.SwitchCamera, CallUI.Action.ToggleCamera, CallUI.Action.HangUp, CallUI.Action.ScreenShare))
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.Camera(),
            CallAction.SwitchCamera(),
            CallAction.HangUp(),
            CallAction.ScreenShare()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun lessOf4Actions_toCallActions_hangUpIsLastPosition() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone, CallUI.Action.SwitchCamera, CallUI.Action.HangUp))
        val result = callFlow.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.SwitchCamera(),
            CallAction.HangUp()
        )
        assertEquals(expected, actual)
    }
}
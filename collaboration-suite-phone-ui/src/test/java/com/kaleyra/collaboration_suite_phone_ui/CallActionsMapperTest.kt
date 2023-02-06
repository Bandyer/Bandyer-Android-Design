package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallActionsMapper.toCallActions
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CallActionsMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>()

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(emptySet())
        val call = MutableStateFlow(callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf<CallAction>()
        assertEquals(expected, actual)
    }

    @Test
    fun allCallActions_toCallActions_mappedCallActions() = runTest {
        every { callMock.state } returns MutableStateFlow(mockk())
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
        val call = MutableStateFlow(callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.Camera(),
            CallAction.SwitchCamera(),
            CallAction.HangUp(),
            CallAction.Chat(),
            CallAction.Whiteboard(),
            CallAction.Audio(),
            CallAction.FileShare(),
            CallAction.ScreenShare(),
        )
        assertEquals(expected, actual)
    }
}
package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WhiteboardMapper.getWhiteboardTextEvents
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WhiteboardMapper.isWhiteboardLoading
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhiteboardMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val whiteboardMock = mockk<Whiteboard>(relaxed = true)

    @Before
    fun setUp() {
        every { callMock.whiteboard } returns whiteboardMock
    }

    @Test
    fun whiteboardStateUnloaded_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded)
        val flow = MutableStateFlow(callMock)
        val actual = flow.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateLoaded_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loaded)
        val flow = MutableStateFlow(callMock)
        val actual = flow.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateLoading_isLoading_true() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loading)
        val flow = MutableStateFlow(callMock)
        val actual = flow.isWhiteboardLoading().first()
        assertEquals(true, actual)
    }

    @Test
    fun whiteboardStateCacheError_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded.Error.Cache)
        val flow = MutableStateFlow(callMock)
        val actual = flow.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateUnknownError_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded.Error.Unknown(""))
        val flow = MutableStateFlow(callMock)
        val actual = flow.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardAddTextEvent_getWhiteboardTextEvents_eventReceived() = runTest {
        val event = Whiteboard.Event.Text.Add(mockk())
        every { whiteboardMock.events } returns MutableStateFlow(event)
        val flow = MutableStateFlow(callMock)
        val actual = flow.getWhiteboardTextEvents().first()
        assertEquals(event, actual)
    }

    @Test
    fun whiteboardEditTextEvent_getWhiteboardTextEvents_eventReceived() = runTest {
        val event = Whiteboard.Event.Text.Edit(oldText = "text", mockk())
        every { whiteboardMock.events } returns MutableStateFlow(event)
        val flow = MutableStateFlow(callMock)
        val actual = flow.getWhiteboardTextEvents().first()
        assertEquals(event, actual)
    }

    @Test
    fun whiteboardShowRequestEvent_getWhiteboardTextEvents_eventNotReceived() = runTest {
        every { whiteboardMock.events } returns MutableStateFlow(Whiteboard.Event.Request.Show)
        val flow = MutableStateFlow(callMock)
        val actual = withTimeoutOrNull(50) {
            flow.getWhiteboardTextEvents().first()
        }
        assertEquals(null, actual)
    }
}
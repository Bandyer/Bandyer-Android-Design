package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUserMessagesProviderTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        CallUserMessagesProvider.start(flowOf(callMock))
    }

    @After
    fun tearDown() {
        CallUserMessagesProvider.dispose()
    }

    @Test
    fun testStart() = runTest {
        CallUserMessagesProvider.start(flowOf(callMock), backgroundScope)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDoubleStart() = runTest {
        val scope = MainScope()
        CallUserMessagesProvider.start(flowOf(callMock), scope)
        CallUserMessagesProvider.start(flowOf(callMock), backgroundScope)
        assertEquals(false, scope.isActive)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDispose() = runTest {
        CallUserMessagesProvider.start(flowOf(callMock), backgroundScope)
        CallUserMessagesProvider.dispose()
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testRecordingStartedUserMessage() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
        val actual = CallUserMessagesProvider.recordingUserMessage.first()
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun recordingStateInitializedWithStopped_recordingStoppedUserMessageNotReceived() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Stopped)
        val result = withTimeoutOrNull(100) {
            CallUserMessagesProvider.recordingUserMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testRecordingStoppedUserMessage() = runTest {
        val recordingState = MutableStateFlow<Call.Recording.State>(Call.Recording.State.Started)
        every { callMock.extras.recording.state } returns recordingState
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            val actual = CallUserMessagesProvider.recordingUserMessage.drop(1).first()
            assert(actual is RecordingMessage.Stopped)
        }
        recordingState.value = Call.Recording.State.Stopped
    }

    @Test
    fun testRecordingFailedUserMessage() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Stopped.Error)
        val actual = CallUserMessagesProvider.recordingUserMessage.first()
        assert(actual is RecordingMessage.Failed)
    }

    @Test
    fun testMutedUserMessage() = runTest {
        val event = mockk<Input.Audio.Event.Request.Mute>()
        val producer = mockk<CallParticipant>()
        val callParticipants = mockk<CallParticipants>()
        val me = mockk<CallParticipant.Me>()
        val streamMock = mockk<Stream.Mutable>()
        val audio = mockk<Input.Audio>()
        every { callMock.participants } returns MutableStateFlow(callParticipants)
        every { producer.displayName } returns MutableStateFlow("username")
        every { event.producer } returns producer
        every { callParticipants.me } returns me
        every { me.streams } returns MutableStateFlow(listOf(streamMock))
        every { streamMock.audio } returns MutableStateFlow(audio)
        every { audio.events } returns MutableStateFlow(event)
        withTimeout(100) {
            CallUserMessagesProvider.mutedUserMessage.first()
        }
    }
}
package com.kaleyra.collaboration_suite_phone_ui

import android.icu.text.AlphabeticIndex.Record
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUserMessagesProviderTest {

    private var callMock = mockk<Call>()

    @Test
    fun testRecordingStartedUserMessage() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
        val provider = CallUserMessagesProvider(flowOf(callMock))
        val actual = provider.recordingUserMessage().first()
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun testRecordingStoppedUserMessage() = runTest {
        val recordingState = MutableStateFlow<Call.Recording.State>(Call.Recording.State.Started)
        every { callMock.extras.recording.state } returns recordingState
        val provider = CallUserMessagesProvider(flowOf(callMock))
        val values = mutableListOf<RecordingMessage>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            provider.recordingUserMessage().toList(values)
        }
        recordingState.value = Call.Recording.State.Stopped
        assert(values[1] is RecordingMessage.Stopped)
    }

    @Test
    fun testRecordingFailedUserMessage() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Stopped.Error)
        val provider = CallUserMessagesProvider(flowOf(callMock))
        val actual = provider.recordingUserMessage().first()
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
        val provider = CallUserMessagesProvider(flowOf(callMock))
        withTimeout(100) {
            provider.mutedUserMessage().first()
        }
    }
}
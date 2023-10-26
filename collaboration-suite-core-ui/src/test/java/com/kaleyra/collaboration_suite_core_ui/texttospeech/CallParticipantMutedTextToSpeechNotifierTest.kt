package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallParticipantMutedTextToSpeechNotifierTest {

    private val callMock = mockk<CallUI>()

    private val proximitySensorMock = mockk<ProximitySensor>()

    private val callTextToSpeechMock = mockk<CallTextToSpeech>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    private val notifier = spyk(CallParticipantMutedTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val streamMock = mockk<Stream.Mutable>()

    private val audioMock = mockk<Input.Audio>()

    private val eventMock = mockk<Input.Audio.Event.Request.Mute>()

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        every { notifier.shouldNotify } returns true
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns participantMeMock
        })
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
        every { streamMock.audio } returns MutableStateFlow(audioMock)
        every { streamMock.id } returns CameraStreamPublisher.CAMERA_STREAM_ID
        every { audioMock.events } returns MutableStateFlow(eventMock)
    }

    @Test
    fun `test participant muted utterance`() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.getString(R.string.kaleyra_call_participant_utterance_muted_by_admin) } returns "text"

        notifier.start(backgroundScope)

        advanceUntilIdle()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_call_participant_utterance_muted_by_admin) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun testDispose() = runTest(UnconfinedTestDispatcher()) {
        notifier.start(backgroundScope)
        notifier.dispose()
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

    @Test
    fun `calling start again disposes previous notifier tts`() = runTest(UnconfinedTestDispatcher()) {
        notifier.start(backgroundScope)
        notifier.start(backgroundScope)
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }
}
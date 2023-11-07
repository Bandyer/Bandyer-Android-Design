package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.mapper.StreamMapper
import com.kaleyra.collaboration_suite_core_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AwaitingParticipantsTextToSpeechNotifierTest {

    private val callMock = mockk<CallUI>()

    private val proximitySensorMock = mockk<ProximitySensor>()

    private val callTextToSpeechMock = mockk<CallTextToSpeech>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    private val notifier = spyk(AwaitingParticipantsTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        mockkObject(StreamMapper)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        every { notifier.shouldNotify } returns true
        every { any<Flow<Call>>().amIWaitingOthers() } returns MutableStateFlow(true)
    }

    @Test
    fun `test i am waiting others utterance`() = runTest {
        every { contextMock.getString(R.string.kaleyra_call_waiting_for_other_participants) } returns "text"

        notifier.start(backgroundScope)

        advanceTimeBy(AwaitingParticipantsTextToSpeechNotifier.AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_call_waiting_for_other_participants) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }

        advanceTimeBy(1)
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_call_waiting_for_other_participants) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test i am waiting others utterance not played`() = runTest {
        every { any<Flow<Call>>().amIWaitingOthers() } returns MutableStateFlow(false)
        every { contextMock.getString(R.string.kaleyra_call_waiting_for_other_participants) } returns "text"

        notifier.start(backgroundScope)

        advanceTimeBy(AwaitingParticipantsTextToSpeechNotifier.AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_call_waiting_for_other_participants) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
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
package com.kaleyra.video_common_ui.texttospeech

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
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
class CallRecordingTextToSpeechNotifierTest {

    private val callMock = mockk<CallUI>()

    private val proximitySensorMock = mockk<ProximitySensor>()

    private val callTextToSpeechMock = mockk<CallTextToSpeech>(relaxed = true)

    private val recordingMock = mockk<Call.Recording>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    private val notifier = spyk(CallRecordingTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        every { notifier.shouldNotify } returns true
    }

    @Test
    fun `test recording on demand utterance`() = runTest(UnconfinedTestDispatcher()) {
        val recordingMock = mockk<Call.Recording>(relaxed = true)
        every { recordingMock.type } returns Call.Recording.Type.OnDemand
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        notifier.start(backgroundScope)
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_call_may_be_recorded) }
    }

    @Test
    fun `test recording on connect utterance`() = runTest(UnconfinedTestDispatcher()) {
        val recordingMock = mockk<Call.Recording>(relaxed = true)
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        notifier.start(backgroundScope)
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) }
    }

    @Test
    fun `test recording started utterance`() = runTest(UnconfinedTestDispatcher()) {
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_started) } returns "text"

        notifier.start(backgroundScope)

        advanceUntilIdle()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_started) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `initial recording stopped state is ignored`() = runTest(UnconfinedTestDispatcher()) {
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Stopped>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) } returns "text"

        notifier.start(backgroundScope)

        advanceUntilIdle()
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording stopped utterance`() = runTest(UnconfinedTestDispatcher()) {
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)

        state.value = mockk<Call.Recording.State.Stopped>()
        advanceUntilIdle()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording error utterance`() = runTest(UnconfinedTestDispatcher()) {
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_failed) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)

        state.value = mockk<Call.Recording.State.Stopped.Error>()
        advanceUntilIdle()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_failed) }
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
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Started>())
        notifier.start(backgroundScope)
        notifier.start(backgroundScope)
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

}
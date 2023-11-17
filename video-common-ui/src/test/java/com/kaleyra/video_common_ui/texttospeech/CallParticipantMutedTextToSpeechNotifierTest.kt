/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.texttospeech

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.mapper.InputMapper
import com.kaleyra.video_common_ui.mapper.InputMapper.toMuteEvents
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        mockkObject(InputMapper)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        every { notifier.shouldNotify } returns true
        every { any<Flow<Call>>().toMuteEvents() } returns MutableStateFlow(mockk())
    }

    @Test
    fun `test participant muted utterance`() = runTest {
        every { contextMock.getString(R.string.kaleyra_call_participant_utterance_muted_by_admin) } returns "text"

        notifier.start(backgroundScope)

        runCurrent()
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
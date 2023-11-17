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
import android.media.AudioManager
import com.kaleyra.video_common_ui.texttospeech.TextToSpeechAudioManager.Companion.TTSVolumePercentage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TextToSpeechAudioManagerTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val audioManagerMock = mockk<AudioManager>(relaxed = true)

    private val textToSpeechAudioManager = TextToSpeechAudioManager(contextMock)

    @Before
    fun setUp() {
        every { contextMock.getSystemService(Context.AUDIO_SERVICE) } returns audioManagerMock
    }

    @Test
    fun testSetVolume() {
        val volume = 5
        textToSpeechAudioManager.setVolume(volume)
        verify(exactly = 1) { audioManagerMock.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0) }
    }

    @Test
    fun testGetVolume() {
        textToSpeechAudioManager.getVolume()
        verify(exactly = 1) { audioManagerMock.getStreamVolume(AudioManager.STREAM_MUSIC) }
    }

    @Test
    fun testGetTargetVolume() {
        val volume = 10
        every { audioManagerMock.getStreamMaxVolume(AudioManager.STREAM_MUSIC) } returns volume
        val actual = textToSpeechAudioManager.getTargetVolume()
        val expected = (volume * TTSVolumePercentage).toInt()
        verify(exactly = 1) { audioManagerMock.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
        assertEquals(expected, actual)
    }
}
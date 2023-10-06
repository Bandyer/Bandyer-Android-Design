package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import com.bandyer.android_audiosession.sounds.CallSound
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

internal class CallTextToSpeechTest {

    private val callTextToSpeech = CallTextToSpeech()

    private val contextMock = mockk<Context>()

    @Before
    fun setUp() {
        mockkConstructor(TextToSpeech::class)
        mockkConstructor(TextToSpeechAudioManager::class)
        mockkObject(ContextRetainer)
        mockkObject(CallSound)
        every { ContextRetainer.context } returns contextMock
        every { anyConstructed<TextToSpeech>().stop() } returns 0
        every { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(any()) } returns 0
        every { anyConstructed<TextToSpeech>().shutdown() } returns Unit
        every { anyConstructed<TextToSpeech>().setLanguage(any()) } returns 0
        every { anyConstructed<TextToSpeechAudioManager>().getVolume() } returns 0
        every { anyConstructed<TextToSpeechAudioManager>().getTargetVolume() } returns 0
        every { anyConstructed<TextToSpeechAudioManager>().setVolume(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `default locale is set as text to speech language`() {
        callTextToSpeech.speak("") {
            verify(exactly = 1) { anyConstructed<TextToSpeech>().language = Locale.getDefault() }
        }
    }

    @Test
    fun `the current volume is lower than the target volume, the target volume is set`() {
        val targetVolume = 10
        every { anyConstructed<TextToSpeechAudioManager>().getVolume() } returns 5
        every { anyConstructed<TextToSpeechAudioManager>().getTargetVolume() } returns targetVolume
        every { anyConstructed<TextToSpeechAudioManager>().setVolume(any()) } returns Unit
        callTextToSpeech.speak("") {
            verify(exactly = 1) { anyConstructed<TextToSpeechAudioManager>().setVolume(targetVolume) }
        }
    }

    @Test
    fun `the current volume is higher than the target volume, the volume is not changed`() {
        val targetVolume = 5
        every { anyConstructed<TextToSpeechAudioManager>().getVolume() } returns 10
        every { anyConstructed<TextToSpeechAudioManager>().getTargetVolume() } returns targetVolume
        every { anyConstructed<TextToSpeechAudioManager>().setVolume(any()) } returns Unit
        callTextToSpeech.speak("") {
            verify(exactly = 0) { anyConstructed<TextToSpeechAudioManager>().setVolume(targetVolume) }
        }
    }

    @Test
    fun `test call sounds are stopped on tts speak`() {
        val currentVolume = 5
        every { anyConstructed<TextToSpeechAudioManager>().getVolume() } returns currentVolume
        every { CallSound.isPlaying() } returns true
        every { CallSound.stop(any(), any()) } answers { firstArg<() -> Unit>().invoke() }
        callTextToSpeech.speak("") {
            verify(exactly = 1) { CallSound.stop(any(), false) }
            verify(exactly = 4) { anyConstructed<TextToSpeechAudioManager>().setVolume(currentVolume) }
        }
    }

    @Test
    fun `test call sounds are stopped on tts`() {
        val currentVolume = 5
        every { anyConstructed<TextToSpeechAudioManager>().getVolume() } returns currentVolume
        every { CallSound.isPlaying() } returns false
        callTextToSpeech.speak("") {
            verify(exactly = 1) { anyConstructed<TextToSpeechAudioManager>().setVolume(currentVolume) }
        }
    }

    @Test
    fun `dispose tts on speak completion if dispose was called with instantly false and tts was speaking`() {
        every { CallSound.isPlaying() } returns false
        every { anyConstructed<TextToSpeech>().isSpeaking } returns true
        callTextToSpeech.dispose()
        callTextToSpeech.speak("") {
            verify(exactly = 1) { anyConstructed<TextToSpeech>().stop() }
            verify(exactly = 1) { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(null) }
            verify(exactly = 1) { anyConstructed<TextToSpeech>().shutdown() }
        }
    }

    @Test
    fun `test dispose instantly true`() {
        callTextToSpeech.speak("")
        callTextToSpeech.dispose(true)
        verify(exactly = 1) { anyConstructed<TextToSpeech>().stop() }
        verify(exactly = 1) { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(null) }
        verify(exactly = 1) { anyConstructed<TextToSpeech>().shutdown() }
    }

    @Test
    fun `test dispose instantly false and tts is not speaking`() {
        every { anyConstructed<TextToSpeech>().isSpeaking } returns false
        callTextToSpeech.speak("")
        callTextToSpeech.dispose(false)
        verify(exactly = 1) { anyConstructed<TextToSpeech>().stop() }
        verify(exactly = 1) { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(null) }
        verify(exactly = 1) { anyConstructed<TextToSpeech>().shutdown() }
    }

    @Test
    fun `test dispose instantly false and tts is speaking`() {
        every { anyConstructed<TextToSpeech>().isSpeaking } returns true
        callTextToSpeech.speak("")
        callTextToSpeech.dispose(false)
        verify(exactly = 0) { anyConstructed<TextToSpeech>().stop() }
        verify(exactly = 0) { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(null) }
        verify(exactly = 0) { anyConstructed<TextToSpeech>().shutdown() }
    }
}
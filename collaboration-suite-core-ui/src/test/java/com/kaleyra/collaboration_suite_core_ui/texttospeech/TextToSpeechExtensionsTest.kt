package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.kaleyra.collaboration_suite_core_ui.texttospeech.TextToSpeechExtensions.DefaultSpeakBundle
import com.kaleyra.collaboration_suite_core_ui.texttospeech.TextToSpeechExtensions.queue
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class TextToSpeechExtensionsTest {

    private val textToSpeechMock = mockk<TextToSpeech>(relaxed = true)

    @Test
    fun `verify speak is called on`() {
        val text = "textToSpeechText"
        textToSpeechMock.queue(text) { }
        verify(exactly = 1) { textToSpeechMock.speak(text, TextToSpeech.QUEUE_ADD, DefaultSpeakBundle, text) }
    }

    @Test
    fun `onCompletion is invoked when the utterance is done`() {
        var listener: UtteranceProgressListener? = null
        val onCompletion: () -> Unit = spyk()
        textToSpeechMock.queue(text = "", onCompletion = onCompletion)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(withArg { listener = it }) }

        listener?.onDone(null)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(null) }
        verify(exactly = 1) { onCompletion.invoke() }
    }

    @Test
    fun `onCompletion is invoked when the utterance get an error 1`() {
        var listener: UtteranceProgressListener? = null
        val onCompletion: () -> Unit = spyk()
        textToSpeechMock.queue(text = "", onCompletion = onCompletion)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(withArg { listener = it }) }

        listener?.onError(null)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(null) }
        verify(exactly = 1) { onCompletion.invoke() }
    }

    @Test
    fun `onCompletion is invoked when the utterance get an error 2`() {
        var listener: UtteranceProgressListener? = null
        val onCompletion: () -> Unit = spyk()
        textToSpeechMock.queue(text = "", onCompletion = onCompletion)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(withArg { listener = it }) }

        listener?.onError(null, 0)
        verify(exactly = 1) { textToSpeechMock.setOnUtteranceProgressListener(null) }
        verify(exactly = 1) { onCompletion.invoke() }
    }
}
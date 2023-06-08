package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import com.bandyer.android_audiosession.sounds.CallSound
import com.kaleyra.collaboration_suite_core_ui.texttospeech.TextToSpeechExtensions.queue
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import java.util.Locale

internal class CallTextToSpeech {

    private var textToSpeech: TextToSpeech? = null

    private var shouldDisposeOnCompletion = false

    private val isSpeaking: Boolean
        get() = textToSpeech?.isSpeaking ?: false

    fun speak(text: String, onReady: (() -> Unit)? = null) {
        val context = ContextRetainer.context
        if (textToSpeech != null) {
            setUpTextToSpeech(context, text)
            onReady?.invoke()
        }
        else {
            textToSpeech = try {
                TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech?.language = Locale.getDefault()
                        setUpTextToSpeech(context, text)
                        onReady?.invoke()
                    } else textToSpeech = null
                }
            } catch (t: Throwable) {
                null
            }
        }
    }

    fun dispose(instantly: Boolean = true) {
        if (instantly || !isSpeaking) internalDispose()
        else shouldDisposeOnCompletion = true
    }
    
    private fun internalDispose() {
        textToSpeech?.stop()
        textToSpeech?.setOnUtteranceProgressListener(null)
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun setUpTextToSpeech(context: Context, text: String) {
        val ttsAudioManager = TextToSpeechAudioManager(context)
        val currentVolume = ttsAudioManager.getVolume()
        val targetVolume = ttsAudioManager.getTargetVolume()
        if (currentVolume < targetVolume) ttsAudioManager.setVolume(targetVolume)

        val enqueue = {
            textToSpeech?.queue(
                text = text,
                onCompletion = {
                    ttsAudioManager.setVolume(currentVolume)
                    if (shouldDisposeOnCompletion) internalDispose()
                }
            )
            Unit
        }
        if (CallSound.isPlaying()) CallSound.stop(onStopped = enqueue, instantly = false)
        else enqueue.invoke()
    }

}


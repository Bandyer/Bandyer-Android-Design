package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

internal object TextToSpeechExtensions {

    val DefaultSpeakBundle = Bundle().apply {
        putString(TextToSpeech.Engine.KEY_PARAM_STREAM, "${AudioManager.STREAM_VOICE_CALL}")
        putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1")
    }

    fun TextToSpeech.queue(
        text: String,
        bundle: Bundle = DefaultSpeakBundle,
        onCompletion: () -> Unit
    ) {
        speak(text, TextToSpeech.QUEUE_ADD, bundle, text)
        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = onCompleted()
            override fun onError(utteranceId: String?, errorCode: Int) = onCompleted()
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = onCompleted()
            private fun onCompleted() {
                setOnUtteranceProgressListener(null)
                onCompletion()
            }
        })
    }

}
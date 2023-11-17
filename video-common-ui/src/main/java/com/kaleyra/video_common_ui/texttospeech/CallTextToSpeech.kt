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
import android.speech.tts.TextToSpeech
import com.bandyer.android_audiosession.sounds.CallSound
import com.kaleyra.video_common_ui.texttospeech.TextToSpeechExtensions.queue
import com.kaleyra.video_utils.ContextRetainer
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


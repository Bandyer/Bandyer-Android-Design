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
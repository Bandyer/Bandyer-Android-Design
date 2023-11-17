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

internal class TextToSpeechAudioManager(context: Context) {

    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    fun setVolume(volume: Int) = audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

    fun getVolume() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    fun getTargetVolume(): Int {
        val musicStreamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = musicStreamMaxVolume * TTSVolumePercentage
        return targetVolume.toInt()
    }

    companion object {
        const val TTSVolumePercentage = .75f
    }
}
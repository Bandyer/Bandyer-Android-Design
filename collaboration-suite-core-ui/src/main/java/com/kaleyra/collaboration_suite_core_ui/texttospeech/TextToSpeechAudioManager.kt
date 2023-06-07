package com.kaleyra.collaboration_suite_core_ui.texttospeech

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
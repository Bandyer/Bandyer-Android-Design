package com.bandyer.video_android_glass_ui.utils

import android.content.Context
import androidx.annotation.IntRange

/**
 *  Manager for the call's audio
 *
 * @constructor
 */
internal class CallAudioManager(context: Context) {

    private val manager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    /**
     * The current call volume. This volume goes from 0 to 5.
     */
    val currentVolume: Int
        get() = manager.getStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL)

    /**
     * Set the call volume. The accepted values are from 0 to 5.
     *
     * @param value Int
     */
    fun setVolume(@IntRange(from = 0, to = 5) value: Int) =
        when {
            value < 0 || value > 5 -> Unit
            value == 0 -> {
                manager.setStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL, 1, 0)
                manager.adjustStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL, android.media.AudioManager.ADJUST_LOWER, 0)
            }
            else -> manager.setStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL, value, 0)
        }
}
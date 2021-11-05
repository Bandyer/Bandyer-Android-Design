package com.bandyer.video_android_glass_ui.utils

import android.content.Context
import android.os.Build
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
     * The max call volume
     */
    val maxVolume = manager.getStreamMaxVolume(android.media.AudioManager.STREAM_VOICE_CALL)

    /**
     * The min call volume
     */
    val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) manager.getStreamMinVolume(android.media.AudioManager.STREAM_VOICE_CALL) else 1

    /**
     * Set the call volume. The accepted values are from 0 to 5.
     *
     * @param value Int
     */
    fun setVolume(value: Int) {
        if(value < minVolume || value > maxVolume) return
        manager.setStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL, value, 0)
    }
}
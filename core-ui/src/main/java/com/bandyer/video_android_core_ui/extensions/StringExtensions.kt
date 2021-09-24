package com.bandyer.video_android_core_ui.extensions

import android.graphics.Color
import java.math.BigInteger
import java.security.MessageDigest

/**
 * String extensions
 */
object StringExtensions {

    /**
     * Return a color based on the given a string
     *
     * @receiver The string
     * @return The color
     */
    fun String.parseToColor(): Int {
        val md = MessageDigest.getInstance("MD5")
        val md5 = BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0').substring(0, 8).takeLast(6)
        return Color.parseColor("#$md5")
    }
}
package com.bandyer.video_android_phone_ui.extensions

import android.graphics.Color
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Give the type of file given a mime type
 *
 * @receiver The file mime type
 * @return The type of file
 */
fun String.getFileTypeFromMimeType(): String {
    val imageTypes = listOf("image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp", "image/x-photoshop")
    val archiveType = listOf("application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar")
    return when {
        imageTypes.contains(this) -> "image"
        archiveType.contains(this) -> "archive"
        else -> "file"
    }
}

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
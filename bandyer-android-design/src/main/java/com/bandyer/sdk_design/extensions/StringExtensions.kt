package com.bandyer.sdk_design.extensions

import android.webkit.MimeTypeMap
import java.util.*

fun String.getMimeType(): String = MimeTypeMap.getFileExtensionFromUrl(this)
    ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.toLowerCase(Locale.ROOT)) }
    ?: ""

fun String.getFileNameFromUrl() = this.substring(this.lastIndexOf('/') + 1)

fun String.getFileTypeFromMimeType(): String {
    val imageTypes = listOf("image/gif", "image/vnd.microsoft.icon", "image/jpeg", "image/png", "image/svg+xml", "image/tiff", "image/webp", "image/x-photoshop")
    val archiveType = listOf("application/zip", "application/x-7z-compressed", "application/x-bzip", "application/x-bzip2", "application/gzip", "application/vnd.rar")
    return when {
        imageTypes.contains(this) -> "image"
        archiveType.contains(this) -> "archive"
        else -> "file"
    }
}
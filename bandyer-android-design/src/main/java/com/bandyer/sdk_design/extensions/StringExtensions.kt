package com.bandyer.sdk_design.extensions

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
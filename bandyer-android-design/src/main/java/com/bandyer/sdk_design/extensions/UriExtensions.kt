package com.bandyer.sdk_design.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import java.io.File
import java.util.*

/**
 * Get mime type
 *
 * @param context Context
 * @return mime type
 */
fun Uri.getMimeType(context: Context): String {
    return kotlin.runCatching {
        if (ContentResolver.SCHEME_CONTENT == this.scheme) {
            val cr: ContentResolver = context.applicationContext.contentResolver
            cr.getType(this)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.ROOT))
        }
    }.getOrNull() ?: ""
}

/**
 * Get file name
 *
 * @param context Context
 * @return name
 */
fun Uri.getFileName(context: Context): String {
    return kotlin.runCatching {
        if (ContentResolver.SCHEME_CONTENT == this.scheme)
            context.contentResolver.query(this, null, null, null, null)?.use {
                if(it.moveToFirst()) it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME)) else null
            }
        else File(this.path!!).name
    }.getOrNull() ?: ""
}

/**
 * Get file size
 *
 * @param context Context
 * @return size
 */
fun Uri.getFileSize(context: Context): Long = kotlin.runCatching {
    if (ContentResolver.SCHEME_CONTENT == this.scheme)
        context.contentResolver.query(this, null, null, null, null)
            .use { if (it?.moveToFirst() == true) it.getLong(it.getColumnIndex(OpenableColumns.SIZE)) else -1L }
    else this.toFile().run { if (exists()) length() else -1L }
}.getOrNull() ?: -1L

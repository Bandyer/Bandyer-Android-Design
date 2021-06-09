package com.bandyer.sdk_design.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

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

fun Uri.getFileName(context: Context): String {
    return kotlin.runCatching {
        var result: String? = null
        if (this.scheme == "content") {
            val cursor = context.contentResolver.query(this, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = this.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        result
    }.getOrNull() ?: ""
}
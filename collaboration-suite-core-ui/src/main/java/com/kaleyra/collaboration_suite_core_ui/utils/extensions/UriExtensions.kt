/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

/**
 * Get mime type
 *
 * @param context Context
 * @return mime type
 */
fun Uri.getMimeType(context: Context): String? = runCatching {
    when (scheme) {
        ContentResolver.SCHEME_CONTENT -> context.applicationContext.contentResolver.getType(this)
        else -> {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase(Locale.ROOT))
        }
    }
}.getOrNull()

/**
 * Get file name
 *
 * @param context Context
 * @return name
 */
fun Uri.getFileName(context: Context): String? = when (scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(context)
    else -> path?.let { File(it) }?.name
}

private fun Uri.getContentFileName(context: Context): String? = runCatching {
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()

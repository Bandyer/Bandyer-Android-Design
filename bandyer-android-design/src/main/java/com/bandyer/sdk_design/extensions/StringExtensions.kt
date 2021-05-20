package com.bandyer.sdk_design.extensions

import android.webkit.MimeTypeMap
import java.util.*

fun String.getMimeType(): String = MimeTypeMap.getFileExtensionFromUrl(this)
    ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.toLowerCase(Locale.ROOT)) }
    ?: ""

package com.bandyer.sdk_design.filesharing.model

import android.content.Context
import android.net.Uri
import com.bandyer.sdk_design.extensions.getFileName
import com.bandyer.sdk_design.extensions.getMimeType
import java.util.*

data class FileInfo(
    val id: String,
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sender: String,
    val creationTime: Long = Date().time,
    val size: Long = -1L
) {
    companion object {
        fun create(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String) = FileInfo(id, uri, uri.getFileName(context), uri.getMimeType(context), sender)
        fun create(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String, creationTime: Long, size: Long) = FileInfo(id, uri, uri.getFileName(context), uri.getMimeType(context), sender, creationTime, size)
    }
}
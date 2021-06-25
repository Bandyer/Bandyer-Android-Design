package com.bandyer.sdk_design.filesharing.model

import android.content.Context
import android.net.Uri
import com.bandyer.sdk_design.extensions.getFileName
import com.bandyer.sdk_design.extensions.getMimeType
import java.util.*

/**
 * FileInfo
 *
 * @property id The id of the file transferred
 * @property uri The uri which specifies the file location
 * @property name The file's name
 * @property mimeType The file's mime type
 * @property sender The user who sent the file
 * @property creationTime The creation time of the file
 * @property size The size of the file
 * @constructor
 */
data class FileData(
    val id: String,
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sender: String,
    val creationTime: Long = Date().time,
    val size: Long = -1L
) {
    companion object {
        fun create(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String) = FileData(id, uri, uri.getFileName(context), uri.getMimeType(context), sender)
        fun create(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String, creationTime: Long, size: Long) = FileData(id, uri, uri.getFileName(context), uri.getMimeType(context), sender, creationTime, size)
    }
}
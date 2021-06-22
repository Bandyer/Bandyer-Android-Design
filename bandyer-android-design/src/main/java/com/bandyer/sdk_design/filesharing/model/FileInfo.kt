package com.bandyer.sdk_design.filesharing.model

import android.net.Uri
import java.util.*

data class FileInfo(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sender: String,
    val creationTime: Long = Date().time,
    val size: Long = -1L
)
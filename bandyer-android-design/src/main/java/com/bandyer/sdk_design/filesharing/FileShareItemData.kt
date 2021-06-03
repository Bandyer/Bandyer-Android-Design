package com.bandyer.sdk_design.filesharing

import android.net.Uri

interface FileShareItemData {
    val id: String
    val startTime: Long
    val totalBytes: Long
    val uri: Uri?
}
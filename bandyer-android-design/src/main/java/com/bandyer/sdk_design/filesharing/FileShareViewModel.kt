package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.io.File

abstract class FileShareViewModel : ViewModel() {
    abstract fun upload(uploadId: String?, context: Context, uri: Uri): String

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(downloadId: String? = null, endpoint: String, file: File): String

    abstract fun cancelDownload(downloadId: String)
}
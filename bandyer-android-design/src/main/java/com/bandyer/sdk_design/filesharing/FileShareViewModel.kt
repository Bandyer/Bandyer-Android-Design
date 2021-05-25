package com.bandyer.sdk_design.filesharing

import androidx.lifecycle.ViewModel
import java.io.File

abstract class FileShareViewModel : ViewModel() {
    abstract fun upload(uploadId: String? = null, file: File, keepFileOnSuccess: Boolean = false): String

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(downloadId: String? = null, endpoint: String, file: File): String

    abstract fun cancelDownload(downloadId: String)
}
package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.io.File

abstract class FileShareViewModel : ViewModel() {
    abstract fun upload(uploadId: String?, context: Context, uri: Uri): String

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(downloadId: String?, endpoint: String, context: Context): String

    abstract fun cancelDownload(downloadId: String)
}
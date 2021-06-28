package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.util.*

/**
 * View model for the file sharing
 */
abstract class FileShareViewModel : ViewModel() {
    abstract fun uploadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun downloadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun cancelFileUpload(uploadId: String)

    abstract fun cancelFileDownload(downloadId: String)

    abstract fun cancelAllFileUploads()

    abstract fun cancelAllFileDownloads()
}

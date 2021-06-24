package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.util.*

abstract class FileShareViewModel : ViewModel() {
    abstract fun upload(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun download(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun cancelUpload(uploadId: String)

    abstract fun cancelDownload(downloadId: String)
}

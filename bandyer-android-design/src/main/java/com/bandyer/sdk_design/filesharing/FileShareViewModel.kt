package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.*
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    val itemsData: ConcurrentHashMap<String, FileTransfer> = ConcurrentHashMap()

    abstract fun upload(context: Context, uri: Uri, sender: String): String

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(context: Context, uri: Uri, sender: String): String

    abstract fun cancelDownload(uploadId: String)
}

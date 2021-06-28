package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.TransferData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * View model for the file sharing
 */
abstract class FileShareViewModel : ViewModel() {

    abstract val itemsData: Map<String, TransferData>

    abstract fun uploadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun downloadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    abstract fun cancelFileUpload(uploadId: String)

    abstract fun cancelFileDownload(downloadId: String)

    abstract fun cancelAllFileUploads()

    abstract fun cancelAllFileDownloads()
}

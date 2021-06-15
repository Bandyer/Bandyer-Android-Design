package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.FileShareItemData
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    val itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    abstract fun upload(uploadId: String?, context: Context, uri: Uri): String

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(downloadId: String?, endpoint: String, context: Context): String

    abstract fun cancelDownload(downloadId: String)
}
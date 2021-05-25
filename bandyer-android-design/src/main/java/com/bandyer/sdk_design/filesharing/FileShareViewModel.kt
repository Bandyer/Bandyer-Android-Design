package com.bandyer.sdk_design.filesharing

import androidx.lifecycle.ViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {

    abstract var itemsData: ConcurrentHashMap<String, FileShareItemData>

    abstract fun upload(uploadId: String, file: File, keepFileOnSuccess: Boolean = false)

    abstract fun cancelUpload(uploadId: String)

    abstract fun download(downloadId: String, endpoint: String, file: File)

    abstract fun cancelDownload(downloadId: String)
}
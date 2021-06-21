package com.bandyer.sdk_design.filesharing

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.*
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    val itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    abstract fun upload(context: Context, uploadData: UploadItemData): UploadData

    abstract fun cancelUpload(uploadData: UploadData)

    abstract fun download(context: Context, downloadData: DownloadItemData): DownloadData

    abstract fun cancelDownload(downloadData: DownloadData)
}

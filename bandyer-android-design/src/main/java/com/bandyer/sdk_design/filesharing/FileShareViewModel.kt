package com.bandyer.sdk_design.filesharing

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.FileShareItemData
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    val itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    abstract fun <T, F> upload(context: Context, info: F): T

    abstract fun <T> cancelUpload(upload: T)

    abstract fun <T, F> download(context: Context, info: F): T

    abstract fun <T> cancelDownload(download: T)
}

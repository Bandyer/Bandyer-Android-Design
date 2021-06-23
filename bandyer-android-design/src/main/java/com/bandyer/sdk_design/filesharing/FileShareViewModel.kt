package com.bandyer.sdk_design.filesharing

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.*
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    abstract val itemsData: ConcurrentHashMap<String, FileShareItemData>

    abstract fun upload(context: Context, itemData: FileShareItemData): FileShareItemData

    abstract fun download(context: Context, itemData: FileShareItemData): FileShareItemData

    abstract fun cancel(itemData: FileShareItemData)
}

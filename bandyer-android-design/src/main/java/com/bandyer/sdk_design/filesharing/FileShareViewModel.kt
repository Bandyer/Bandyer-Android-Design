package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.*
import java.util.concurrent.ConcurrentHashMap

abstract class FileShareViewModel : ViewModel() {
    val itemsData: ConcurrentHashMap<String, FileTransfer> = ConcurrentHashMap()

    abstract fun upload(context: Context, transfer: FileTransfer): FileTransfer

    abstract fun download(context: Context, transfer: FileTransfer): FileTransfer

    abstract fun cancel(transfer: FileTransfer)
}

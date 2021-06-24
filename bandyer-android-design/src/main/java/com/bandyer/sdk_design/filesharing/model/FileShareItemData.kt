package com.bandyer.sdk_design.filesharing.model

import android.net.Uri

data class FileShareItemData(val info: FileInfo, val state: State, val type: Type){

    sealed class State {
        object Pending : State()
        data class OnProgress(val bytesTransferred: Long) : State()
        data class Success(val uri: Uri) : State()
        data class Error(val throwable: Throwable) : State()
        object Cancelled: State()
    }

    sealed class Type {
        object Upload: Type()
        object Download: Type()
        object DownloadAvailable: Type()
    }
}
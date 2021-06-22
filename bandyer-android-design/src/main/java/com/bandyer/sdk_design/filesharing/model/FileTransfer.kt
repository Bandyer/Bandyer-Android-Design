package com.bandyer.sdk_design.filesharing.model

import android.net.Uri

interface FileTransfer {

    val info: FileInfo
    val state: State

    sealed class State {
        object Pending : State()
        data class OnProgress(val bytesTransferred: Long) : State()
        data class Success(val uri: Uri) : State()
        data class Error(val throwable: Throwable) : State()
        object Cancelled : State()
    }
}

data class Upload(override val info: FileInfo, override val state: FileTransfer.State) : FileTransfer

data class Download(override val info: FileInfo, override val state: FileTransfer.State) : FileTransfer

data class DownloadAvailable(override val info: FileInfo) : FileTransfer {
    override val state = FileTransfer.State.Pending
}
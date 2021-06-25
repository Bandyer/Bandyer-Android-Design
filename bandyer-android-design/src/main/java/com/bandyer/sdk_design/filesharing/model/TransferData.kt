package com.bandyer.sdk_design.filesharing.model

import android.net.Uri

/**
 * TransferData
 *
 * @property data The file's data
 * @property state State The transfer's state
 * @property type Type The transfer's type
 * @constructor
 */
data class TransferData(val data: FileData, val state: State, val type: Type){

    /**
     * The states of the transfer
     */
    sealed class State {
        object Pending : State()
        data class OnProgress(val bytesTransferred: Long) : State()
        data class Success(val uri: Uri) : State()
        data class Error(val throwable: Throwable) : State()
        object Cancelled: State()
    }

    /**
     * The types of the transfer
     */
    sealed class Type {
        object Upload: Type()
        object Download: Type()
        object DownloadAvailable: Type()
    }
}
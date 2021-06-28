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
data class TransferData(
    val data: FileData,
    val state: State,
    val type: Type
) {

    var bytesTransferred: Long = 0L
    var successUri: Uri? = null

    /**
     * The states of the transfer
     */
    sealed class State {
        object Available : State()
        object Pending : State()
        object OnProgress : State()
        object Success : State()
        object Error : State()
        object Cancelled : State()
    }

    /**
     * The types of the transfer
     */
    sealed class Type {
        object Upload : Type()
        object Download : Type()
    }
}
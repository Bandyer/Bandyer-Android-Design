/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_phone_ui.filesharing.model

import android.content.Context
import android.net.Uri
import com.bandyer.video_android_phone_ui.extensions.getFileName
import com.bandyer.video_android_phone_ui.extensions.getMimeType
import java.util.*

/**
 *
 * @property context Context
 * @property id The id of the file transferred
 * @property uri The uri which specifies the file location
 * @property name The file's name
 * @property mimeType The file's mime type
 * @property sender The user who sent the file
 * @property creationTime The creation time of the file
 * @property bytesTransferred The bytes of the file transferred
 * @property size The size of the file
 * @property successUri The
 * @property state The transfer's state
 * @property type The transfer's type
 * @constructor
 */
data class TransferData(
    val context: Context,
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String = uri.getFileName(context),
    val mimeType: String = uri.getMimeType(context),
    val sender: String,
    val creationTime: Long = Date().time,
    val bytesTransferred: Long = 0L,
    val size: Long = -1L,
    val successUri: Uri? = null,
    val state: State,
    val type: Type
) {
    /**
     * The states of the transfer
     */
    sealed class State {
        /**
         * The file is available to download
         */
        object Available : State()

        /**
         * The file transfer is pending
         */
        object Pending : State()

        /**
         * The file transfer is on progress
         */
        object OnProgress : State()

        /**
         * The file transfer has been successful
         */
        object Success : State()

        /**
         * An error occurred during the file transfer
         */
        object Error : State()

        /**
         * The file transfer has been cancelled
         */
        object Cancelled : State()
    }

    /**
     * The types of the transfer
     */
    sealed class Type {
        /**
         * The file transfer is an upload
         */
        object Upload : Type()

        /**
         * The file transfer is a download
         */
        object Download : Type()
    }

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is TransferData) return false
//
//        if (id != other.id) return false
//        if (uri != other.uri) return false
//        if (name != other.name) return false
//        if (mimeType != other.mimeType) return false
//        if (sender != other.sender) return false
//        if (creationTime != other.creationTime) return false
//        if (bytesTransferred != other.bytesTransferred) return false
//        if (size != other.size) return false
//        if (successUri != other.successUri) return false
//        if (state != other.state) return false
//        if (type != other.type) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = id.hashCode()
//        result = 31 * result + bytesTransferred.hashCode()
//        result = 31 * result + state.hashCode()
//        return result
//    }
}
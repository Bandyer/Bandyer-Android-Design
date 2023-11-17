/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.fileshare.filepick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal interface FilePickProvider {
    val fileUri: Flow<Uri>
}

internal class FilePickBroadcastReceiver : BroadcastReceiver() {

    companion object : FilePickProvider {
        const val ACTION_FILE_PICK_EVENT = "com.kaleyra.collaboration_suite_phone_ui.FILE_PICK_EVENT_ACTION"

        private val mUri: MutableSharedFlow<Uri> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        override val fileUri: Flow<Uri> = mUri
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FILE_PICK_EVENT) return
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("uri", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("uri")
        } ?: return
        mUri.tryEmit(uri)
    }
}
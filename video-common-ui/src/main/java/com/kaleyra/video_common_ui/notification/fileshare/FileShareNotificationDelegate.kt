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

package com.kaleyra.video_common_ui.notification.fileshare

import android.app.Notification
import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video_common_ui.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

internal interface FileShareNotificationDelegate {

    companion object {
        const val EXTRA_DOWNLOAD_ID = "com.kaleyra.video_common_ui.EXTRA_DOWNLOAD_ID"
    }

    fun syncFileShareNotification(
        context: Context,
        call: Call,
        activityClazz: Class<*>,
        scope: CoroutineScope
    ) {
        var lastNotifiedDownload: SharedFile? = null
        val me = call.participants.value.me
        call.sharedFolder.files
            .transform { files ->
                val lastDownload = files.lastOrNull { file -> file.sender.userId != me?.userId } ?: return@transform
                if (lastNotifiedDownload != lastDownload) emit(lastDownload)
                lastNotifiedDownload = lastDownload
            }
            .onEach {
                if (!FileShareVisibilityObserver.isDisplayed.value) {
                    val notification = buildNotification(context, call, it, activityClazz)
                    NotificationManager.notify(it.id.hashCode(), notification)
                }
            }
            .onCompletion {
                call.sharedFolder.files.value.forEach {
                    NotificationManager.cancel(it.id.hashCode())
                }
            }
            .launchIn(scope)
    }

    private suspend fun buildNotification(context: Context, call: Call, sharedFile: SharedFile, activityClazz: Class<*>): Notification {
        val participants = call.participants.first()
        val participant = participants.others.firstOrNull { it.userId == sharedFile.sender.userId }
        val username = participant?.displayName?.first() ?: ""
        return NotificationManager.buildIncomingFileNotification(context, username, sharedFile.id, activityClazz)
    }
}
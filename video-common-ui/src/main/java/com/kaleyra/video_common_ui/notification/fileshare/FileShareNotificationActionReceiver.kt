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

import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.KaleyraVideoBroadcastReceiver
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileShareNotificationActionReceiver internal constructor(val dispatcher: CoroutineDispatcher = Dispatchers.IO): KaleyraVideoBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * ActionHangUp
         */
        const val ACTION_DOWNLOAD = "com.kaleyra.video_common_ui.DOWNLOAD"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(dispatcher).launch {
            requestConfigure().let {
                if (!it) return@let context.goToLaunchingActivity()
                KaleyraVideo.onCallReady(this) { call ->
                    when (intent.extras?.getString("notificationAction")) {
                        ACTION_DOWNLOAD -> {
                            val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return@onCallReady
                            call.sharedFolder.download(downloadId)
                            NotificationManager.cancel(downloadId.hashCode())
                        }
                        else -> Unit
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
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

package com.kaleyra.video_common_ui.notification

import android.content.Context
import android.content.Intent
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.KaleyraVideoBroadcastReceiver
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationDelegate.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The call notification broadcast receiver, it handles the answer and hang up events
 */
class CallNotificationActionReceiver : KaleyraVideoBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * ActionAnswer
         */
        const val ACTION_ANSWER = "com.kaleyra.video_common_ui.ANSWER"

        /**
         * ActionHangUp
         */
        const val ACTION_HANGUP = "com.kaleyra.video_common_ui.HANGUP"

        /**
         * ActionStopScreenShare
         */
        const val ACTION_STOP_SCREEN_SHARE = "com.kaleyra.video_common_ui.STOP_SCREEN_SHARE"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val notificationAction = intent.extras?.getString("notificationAction")
            if (notificationAction == ACTION_HANGUP) {
                NotificationManager.cancel(CALL_NOTIFICATION_ID)
            }
            requestConfigure().let {
                if (!it) {
                    NotificationManager.cancel(CALL_NOTIFICATION_ID)
                    return@let ContextRetainer.context.goToLaunchingActivity()
                }
                KaleyraVideo.onCallReady(this) { call ->
                    when (notificationAction) {
                        ACTION_ANSWER -> call.connect()
                        ACTION_HANGUP -> call.end()
                        ACTION_STOP_SCREEN_SHARE -> {
                            val screenShareInputs =
                                call.inputs.availableInputs.value
                                    .filter { input -> input is Input.Video.Screen || input is Input.Video.Application }
                                    .filter { input -> input.enabled.value }

                            val me = call.participants.value.me ?: return@onCallReady
                            val streams = me.streams.value
                            val screenShareStreams = streams
                                .filter { stream ->
                                    stream.video.value is Input.Video.Application || stream.video.value is Input.Video.Screen
                                }
                            screenShareStreams.forEach { stream -> me.removeStream(stream) }
                            screenShareInputs.forEach { input -> input.tryDisable() }
                        }

                        else -> Unit
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
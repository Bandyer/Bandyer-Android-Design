/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * The call notification broadcast receiver, it handles the answer and hang up events
 */
class CallNotificationActionReceiver: BroadcastReceiver() {

    /**
     * ActionDelegate. Responsible to handle the behaviour on notification action tap
     */
    interface ActionDelegate {
        fun onAnswerAction()
        fun onHangUpAction()
        fun onScreenShareAction()
    }

    companion object {
        /**
         * ActionAnswer
         */
        const val ACTION_ANSWER = "com.kaleyra.collaboration_suite_core_ui.ANSWER"
        /**
         * ActionHangUp
         */
        const val ACTION_HANGUP = "com.kaleyra.collaboration_suite_core_ui.HANGUP"
        /**
         * ActionStopScreenShare
         */
        const val ACTION_STOP_SCREEN_SHARE = "com.kaleyra.collaboration_suite_core_ui.STOP_SCREEN_SHARE"
        /**
         * The call action notification delegate
         */
        var actionDelegate: ActionDelegate? = null
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return

        when (intent.action) {
            ACTION_ANSWER -> actionDelegate?.onAnswerAction()
            ACTION_HANGUP -> actionDelegate?.onHangUpAction()
            ACTION_STOP_SCREEN_SHARE -> actionDelegate?.onScreenShareAction()
            else -> Unit
        }
    }
}
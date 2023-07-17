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

package com.kaleyra.collaboration_suite_core_ui.call

import android.app.Notification
import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isOngoing
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isOutgoing
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

/**
 * CallNotificationDelegate. It is responsible of syncing the call's notifications with the call
 */
interface CallNotificationDelegate {

    /**
     * @suppress
     */
    companion object {
        /**
         * The global call notification id
         */
        const val CALL_NOTIFICATION_ID = 22
    }

    /**
     * Show the notification
     *
     * @param notification The notification
     */
    fun showNotification(notification: Notification) =
        NotificationManager.notify(CALL_NOTIFICATION_ID, notification)

    /**
     * Clear the notification
     */
    fun clearNotification() = NotificationManager.cancel(CALL_NOTIFICATION_ID)

    /**
     * Sync the notifications with the call
     *
     * @param call The call
     * @param activityClazz The call activity class
     * @param scope The coroutine scope
     */
    fun syncCallNotification(
        call: Call,
        activityClazz: Class<*>,
        scope: CoroutineScope
    ) {
        combine(call.state, call.participants) { callState, participants ->
            ContactDetailsManager.refreshContactDetails(*participants.list.map { it.userId }.toTypedArray())
            val notification = buildNotification(call, participants, activityClazz)
            if (notification != null) showNotification(notification)
            callState
        }
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { clearNotification() }
            .launchIn(scope)
    }

    private suspend fun buildNotification(
        call: Call,
        participants: CallParticipants,
        activityClazz: Class<*>
    ): Notification? {
        val context = ContextRetainer.context
        val isGroupCall = participants.others.count() > 1
        val enableCallStyle = !DeviceUtils.isSmartGlass
        val callerDescription = participants.creator()?.combinedDisplayName?.firstOrNull() ?: ""
        val calleeDescription = participants.others.map { it.combinedDisplayName.firstOrNull() ?: Uri.EMPTY }.joinToString()

        return when {
            call.isIncoming() -> {
                NotificationManager.buildIncomingCallNotification(
                    callerDescription,
                    isGroupCall,
                    activityClazz,
                    !AppLifecycle.isInForeground.value || context.isSilent(),
                    enableCallStyle = enableCallStyle
                )
            }
            call.isOutgoing() -> {
                NotificationManager.buildOutgoingCallNotification(
                    calleeDescription,
                    isGroupCall,
                    activityClazz,
                    enableCallStyle = enableCallStyle
                )
            }
            call.isOngoing() -> {
                NotificationManager.buildOngoingCallNotification(
                    calleeDescription,
                    participants.creator() == null,
                    isGroupCall,
                    call.extras.recording is Call.Recording.OnConnect,
                    isSharingScreen = false,
                    call.state.value is Call.State.Connecting,
                    activityClazz,
                    enableCallStyle = enableCallStyle
                )
            }
            else -> null
        }
    }
}
package com.kaleyra.collaboration_suite_core_ui.call

import android.app.Notification
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

interface CallNotificationDelegate {

    var callActivityClazz: Class<*>

    fun syncCallNotification(call: Call, context: Context, coroutineScope: CoroutineScope) {
        val participants = call.participants.value
        val isGroupCall = participants.others.count() > 1
        val callerDescription = usersDescription.name(listOf(callParticipants().creator()?.userId ?: ""))
        val calleeDescription =
            usersDescription.name(callParticipants().others.map { it.userId })
        if (participants.me != participants.creator())
            showIncomingCallNotification(
                usersDescription = callerDescription,
                isGroupCall = isGroupCall,
                isHighPriority = !mIsAppInForeground || context.isSilent(),
                moveToForeground = mIsAppInForeground
            )

        call.state
            .onEach {
                when {
                    it is Call.State.Connecting && participants.me == participants.creator() -> onOutgoingCall()
                    it is Call.State.Connecting || it is Call.State.Connected -> onOngoingCall(it is Call.State.Connecting)
                }
            }
            .takeWhile { it !is Call.State.Connected }
            .launchIn(coroutineScope)
    }

    fun showNotification(notification: Notification, showInForeground: Boolean)

    private fun showIncomingCallNotification(
        usersDescription: String,
        isGroupCall: Boolean,
        isHighPriority: Boolean,
        moveToForeground: Boolean
    ) {
        val notification = NotificationManager.buildIncomingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            activityClazz = callActivityClazz,
            isHighPriority = isHighPriority
        )
        showNotification(notification, moveToForeground)
    }

    private fun showOutgoingCallNotification(usersDescription: String, isGroupCall: Boolean) {
        val notification = NotificationManager.buildOutgoingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            activityClazz = callActivityClazz
        )
        showNotification(notification, true)
    }

    private fun showOnGoingCallNotification(
        usersDescription: String,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        isConnecting: Boolean
    ) {
        val notification = NotificationManager.buildOngoingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            isCallRecorded = isCallRecorded,
            isSharingScreen = false,
            isConnecting = isConnecting,
            activityClazz = callActivityClazz
        )
        showNotification(notification, true)
    }
}
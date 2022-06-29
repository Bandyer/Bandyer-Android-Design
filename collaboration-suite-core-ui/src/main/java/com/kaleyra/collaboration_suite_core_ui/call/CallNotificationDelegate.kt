package com.kaleyra.collaboration_suite_core_ui.call

import android.app.Notification
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

/**
 * CallNotificationDelegate. It is responsible of syncing the call's notifications with the call
 */
interface CallNotificationDelegate : LifecycleOwner {

    /**
     * Show the notification
     *
     * @param notification The notification
     * @param showInForeground True of the notification should be coupled to a foreground service, false otherwise
     */
    fun showNotification(notification: Notification, showInForeground: Boolean)

    /**
     * Clear the notification
     */
    fun clearNotification()

    /**
     * Sync the notifications with the call
     *
     * @param call The call
     * @param usersDescription The usersDescription
     * @param activityClazz The call activity class
     */
    fun syncCallNotification(
        call: Call,
        usersDescription: UsersDescription,
        activityClazz: Class<*>
    ) {
        var lastState: Call.State? = null
        var isServiceInForeground = false
        combine(call.state, AppLifecycle.isInForeground) { state, isInForeground ->
            if (lastState == state && isServiceInForeground) return@combine state
            lastState = state
            if (isInForeground) isServiceInForeground = true
            val notification =
                buildNotification(call, usersDescription, activityClazz, isServiceInForeground) ?: return@combine state
            showNotification(notification, isServiceInForeground)
            state
        }
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { clearNotification() }
            .launchIn(lifecycleScope)
    }

    private suspend fun buildNotification(
        call: Call,
        usersDescription: UsersDescription,
        activityClazz: Class<*>,
        isAppInForeground: Boolean
    ): Notification? {
        val context = ContextRetainer.context
        val participants = call.participants.value
        val isGroupCall = participants.others.count() > 1
        val callerDescription = usersDescription.name(listOf(participants.creator()?.userId ?: ""))
        val calleeDescription = usersDescription.name(participants.others.map { it.userId })

        return when {
            call.isIncoming() -> {
                NotificationManager.buildIncomingCallNotification(
                    callerDescription,
                    isGroupCall,
                    activityClazz,
                    !isAppInForeground || context.isSilent()
                )
            }
            call.isOutgoing() -> {
                NotificationManager.buildOutgoingCallNotification(
                    calleeDescription,
                    isGroupCall,
                    activityClazz
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
                    activityClazz
                )
            }
            else -> null
        }
    }

    private fun Call.isIncoming() =
        state.value is Call.State.Disconnected && participants.value.let { it.creator() != it.me && it.creator() != null }

    private fun Call.isOutgoing() =
        state.value is Call.State.Connecting && participants.value.let { it.creator() == it.me }

    private fun Call.isOngoing() =
        state.value is Call.State.Connecting || state.value is Call.State.Connected || participants.value.creator() == null
}
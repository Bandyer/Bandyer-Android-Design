package com.kaleyra.collaboration_suite_core_ui.call

import android.app.Notification
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

/**
 * CallNotificationDelegate. It is responsible of syncing the call's notifications with the call's state.
 *
 * @property isAppInForeground Boolean
 */
interface CallNotificationDelegate : LifecycleOwner {

    /**
     * Flag which tells if the app is in foreground
     */
    val isAppInForeground: Boolean

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
     * Move the current shown notification to foreground
     *
     * @param call The call
     * @param usersDescription The usersDescription
     * @param activityClazz The call activity class
     */
    suspend fun moveNotificationToForeground(
        call: Call,
        usersDescription: UsersDescription,
        activityClazz: Class<*>
    ) {
        val participants = call.participants.value
        val isGroupCall = participants.others.count() > 1
        val callerDescription = usersDescription.name(listOf(participants.creator()?.userId ?: ""))
        val calleeDescription = usersDescription.name(participants.others.map { it.userId })

        val notification = when {
            call.isIncoming() -> {
                NotificationManager.buildIncomingCallNotification(
                    callerDescription,
                    participants.others.count() > 1,
                    activityClazz,
                    false
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
        } ?: return

        showNotification(notification, true)
    }

    /**
     * Sync the notifications with the call's state
     *
     * @param context The context
     * @param call The call
     * @param usersDescription The usersDescription
     * @param activityClazz The call activity class
     */
    suspend fun syncNotificationWithCallState(
        context: Context,
        call: Call,
        usersDescription: UsersDescription,
        activityClazz: Class<*>
    ) {
        val participants = call.participants.value
        val isGroupCall = participants.others.count() > 1
        val callerDescription = usersDescription.name(listOf(participants.creator()?.userId ?: ""))
        val calleeDescription = usersDescription.name(participants.others.map { it.userId })

        if (call.isIncoming()) {
            val notification = NotificationManager.buildIncomingCallNotification(
                callerDescription,
                isGroupCall,
                activityClazz,
                !isAppInForeground || context.isSilent()
            )
            showNotification(notification, isAppInForeground)
        }

        call.state
            .onEach {
                when {
                    call.isOutgoing() -> {
                        val notification = NotificationManager.buildOutgoingCallNotification(
                            calleeDescription,
                            isGroupCall,
                            activityClazz
                        )
                        showNotification(notification, isAppInForeground)
                    }
                    call.isOngoing() -> {
                        val notification = NotificationManager.buildOngoingCallNotification(
                            calleeDescription,
                            participants.creator() == null,
                            isGroupCall,
                            call.extras.recording is Call.Recording.OnConnect,
                            isSharingScreen = false,
                            it is Call.State.Connecting,
                            activityClazz
                        )
                        showNotification(notification, isAppInForeground)
                    }
                }
            }
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { clearNotification() }
            .launchIn(lifecycleScope)
    }

    private fun Call.isIncoming() =
        state.value is Call.State.Disconnected && participants.value.let { it.creator() != it.me && it.creator() != null }

    private fun Call.isOutgoing() =
        state.value is Call.State.Connecting && participants.value.let { it.creator() == it.me }

    private fun Call.isOngoing() =
        (state.value is Call.State.Connecting || state.value is Call.State.Connected) && participants.value.let { it.creator() == null }
}
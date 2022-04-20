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

interface CallNotificationDelegate : DefaultLifecycleObserver, LifecycleOwner {

    val isAppInForeground: Boolean

    fun showNotification(notification: Notification, showInForeground: Boolean)

    fun clearNotification()

    suspend fun moveNotificationToForeground(
        call: Call,
        usersDescription: UsersDescription,
        activityClazz: Class<*>
    ) {
        val participants = call.participants.value
        val isGroupCall = participants.others.count() > 1
        val callerDescription = usersDescription.name(listOf(participants.creator()?.userId ?: ""))
        val calleeDescription = usersDescription.name(participants.others.map { it.userId })

        when {
            call.isIncoming() -> {
                if (call.state.value is Call.State.Disconnected && participants.me != participants.creator()) {
                    val notification = NotificationManager.buildIncomingCallNotification(
                        callerDescription,
                        participants.others.count() > 1,
                        activityClazz,
                        false
                    )
                    showNotification(notification, true)
                }
            }
            call.isOutgoing() -> {
                val notification = NotificationManager.buildOutgoingCallNotification(
                    calleeDescription,
                    isGroupCall,
                    activityClazz
                )
                showNotification(notification, true)
            }
            call.isOngoing() -> {
                val notification = NotificationManager.buildOngoingCallNotification(
                    calleeDescription,
                    isGroupCall,
                    call.extras.recording is Call.Recording.OnConnect,
                    isSharingScreen = false,
                    call.state.value is Call.State.Connecting,
                    activityClazz
                )
                showNotification(notification, true)
            }
        }
    }

    suspend fun syncNotificationWithCall(
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
        state.value is Call.State.Disconnected && participants.value.me != participants.value.creator()

    private fun Call.isOutgoing() =
        state.value is Call.State.Connecting && participants.value.me == participants.value.creator()

    private fun Call.isOngoing() =
        state.value is Call.State.Connecting || state.value is Call.State.Connected
}
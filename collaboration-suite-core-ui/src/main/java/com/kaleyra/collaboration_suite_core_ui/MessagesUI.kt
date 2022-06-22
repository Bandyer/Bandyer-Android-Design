package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.Messages
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationMessage
import com.kaleyra.collaboration_suite_core_ui.notification.CustomChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MessagesUI(
    messages: Messages,
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivity: Class<*>? = null
) : Messages by messages {

    suspend fun showUnreadMsgs(chatId: String, loggedUserId: String) {
        chatCustomNotificationActivity?.let {
            showCustomInAppNotification(
                chatId,
                loggedUserId,
                it
            )
        } ?: showNotification(chatId, loggedUserId)
    }

    private fun showNotification(chatId: String, loggedUserId: String) = MainScope().launch {
        val messages = other.filter { it.state.value is Message.State.Received }
            .map { it.toChatNotificationMessage() }.sortedBy { it.timestamp }
        val notification = NotificationManager.buildChatNotification(
            loggedUserId,
            CollaborationUI.usersDescription.name(listOf(loggedUserId)),
            CollaborationUI.usersDescription.image(listOf(loggedUserId)),
            chatId,
            messages,
            chatActivityClazz
        )
        NotificationManager.notify(chatId.hashCode(), notification)
    }

    private suspend fun OtherMessage.toChatNotificationMessage() = ChatNotificationMessage(
        creator.userId,
        CollaborationUI.usersDescription.name(listOf(creator.userId)),
        CollaborationUI.usersDescription.image(listOf(creator.userId)),
        (content as? Message.Content.Text)?.message ?: "",
        creationDate.time
    )

    private suspend fun showCustomInAppNotification(
        chatId: String,
        loggedUserId: String,
        chatCustomNotificationActivity: Class<*>,
    ) {
        val message = other.firstOrNull { it.state.value is Message.State.Received } ?: return

        if (AppLifecycle.isInForeground.value) {
            CustomChatNotificationManager.notify(chatId, chatCustomNotificationActivity)
            return
        }

        val notification = NotificationManager.buildChatNotification(
            loggedUserId,
            CollaborationUI.usersDescription.name(listOf(loggedUserId)),
            CollaborationUI.usersDescription.image(listOf(loggedUserId)),
            chatId,
            listOf(message.toChatNotificationMessage()),
            chatActivityClazz,
            chatCustomNotificationActivity
        )

        NotificationManager.cancel(FULLSCREEN_NOTIFICATION_TAG, FULLSCREEN_NOTIFICATION_ID)
        NotificationManager.notify(
            FULLSCREEN_NOTIFICATION_TAG,
            FULLSCREEN_NOTIFICATION_ID,
            notification
        )

    }

    private companion object {
        const val FULLSCREEN_NOTIFICATION_TAG = "customFullScreenNotification"
        const val FULLSCREEN_NOTIFICATION_ID = 999
    }

}
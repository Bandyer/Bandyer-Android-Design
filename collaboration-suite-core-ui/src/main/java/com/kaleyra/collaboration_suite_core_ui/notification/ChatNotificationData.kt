package com.kaleyra.collaboration_suite_core_ui.notification

import android.net.Uri

/**
 * NotificationData
 *
 * @property name The user name
 * @property userId The user identifier
 * @property message The message sent by the user
 * @property imageUri The avatar image uri
 * @constructor
 */
data class ChatNotificationData(
    val name: String,
    val userId: String,
    val message: String,
    val imageUri: Uri = Uri.EMPTY,
    val usersList: List<String>,
    val timestamp: Long = 0L,
    val channelId: String = ""
)
package com.kaleyra.collaboration_suite_core_ui.notification

import android.net.Uri

/**
 * The chat notification message
 *
 * @property userId The logged user id
 * @property username The logger user name
 * @property avatar The logger user avatar
 * @property text The content text
 * @property timestamp The message timestamp
 * @constructor
 */
internal data class ChatNotificationMessage(
    val userId: String,
    val username: String,
    val avatar: Uri,
    val text: String,
    val timestamp: Long
)
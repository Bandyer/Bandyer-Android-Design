package com.kaleyra.collaboration_suite_core_ui.notification

import android.net.Uri

data class ChatNotificationMessage(
    val username: String,
    val userId: String,
    val avatar: Uri,
    val text: String,
    val timestamp: Long
)
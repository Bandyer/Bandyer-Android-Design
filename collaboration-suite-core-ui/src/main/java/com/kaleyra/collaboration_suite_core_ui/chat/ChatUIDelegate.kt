package com.kaleyra.collaboration_suite_core_ui.chat

import com.bandyer.android_chat_sdk.api.ChatChannel
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.SharedFlow

/**
 * Chat UI delegate
 */
interface ChatUIDelegate {
    /**
     * Chat channel
     */
    val channel: SharedFlow<ChatChannel>

    /**
     * Users description
     */
    val usersDescription: UsersDescription
}
package com.kaleyra.collaboration_suite_core_ui.chat

import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.SharedFlow

/**
 * Chat UI delegate
 */
interface ChatUIDelegate {
    /**
     * Chat channel
     */
    val chat: SharedFlow<Chat>

    /**
     * Users description
     */
    val chatUsersDescription: UsersDescription
}
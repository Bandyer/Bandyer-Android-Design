package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.StateFlow

/**
 * Call UI delegate
 */
interface ChatUIDelegate {
    /**
     * Call
     */
    val chats: StateFlow<List<ChatUI>>

    /**
     * Users description
     */
    val usersDescription: UsersDescription
}

/**
 * The chat delegate
 *
 * @constructor
 */
class ChatDelegate(
    override val chats: StateFlow<List<ChatUI>>,
    override val usersDescription: UsersDescription
) : ChatUIDelegate
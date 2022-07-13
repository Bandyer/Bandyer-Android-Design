package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.utils.extensions.mapToStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The chat box UI
 *
 * @property chatBox The ChatBox delegate
 * @property userId The logged user id
 * @property chatActivityClazz The chat activity Class<*>
 * @property chatCustomNotificationActivityClazz The custom chat notification activity Class<*>
 * @constructor
 */
class ChatBoxUI(
    private val chatBox: ChatBox,
    private val userId: String,
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null
//    private val logger: PriorityLogger? = null
) : ChatBox by chatBox {

    private var chatScope = CoroutineScope(Dispatchers.IO)

    private var lastMessagePerChat: HashMap<String, String> = hashMapOf()

    private var mappedChats: List<ChatUI> = listOf()

    /**
     * @suppress
     */
    override val chats: StateFlow<List<ChatUI>> = chatBox.chats.mapToStateFlow(chatScope) { chats -> chats.map { getOrCreateChatUI(it) } }

    /**
     * WithUI flag, set to true to show the chat notifications, false otherwise
     */
    var withUI: Boolean = true

    init {
        listenToMessages()
    }

    /**
     * @suppress
     */
    override fun connect() = chatBox.connect()

    /**
     * @suppress
     */
    override fun disconnect(clearSavedData: Boolean) = chatBox.disconnect(clearSavedData)

    internal fun dispose(clearSavedData: Boolean) {
        disconnect(clearSavedData)
        chatScope.cancel()
    }
    /**
     * Show the chat ui
     * @param context context to bind the chat ui
     * @param chat The chat object that should be shown.
     */
    fun show(context: Context, chat: ChatUI) =
        UIProvider.showChat(context, chatActivityClazz, chat.participants.value.others.first().userId)

    /**
     * @suppress
     */
    override fun create(user: User): ChatUI = getOrCreateChatUI(user)

    /**
     * Given a user, open a chat ui.
     * @param context launching context of the chat ui
     * @param user The user with whom you want to chat.
     */
    fun chat(context: Context, user: User): ChatUI = create(user).apply { show(context, this) }

    private fun listenToMessages() {
        var msgsScope: CoroutineScope? = null
        chats.onEach { chats ->
            msgsScope?.cancel()
            msgsScope = CoroutineScope(SupervisorJob(chatScope.coroutineContext[Job]))
            chats.forEach { chat ->
                chat.messages.onEach messagesUI@{
                    if (!withUI) return@messagesUI
                    val lastMessage = it.other.firstOrNull { it.state.value is Message.State.Received }
                    if (lastMessage == null || lastMessagePerChat[chat.id] == lastMessage.id) return@messagesUI
                    lastMessagePerChat[chat.id] = lastMessage.id
                    it.showUnreadMsgs(chat.id, userId)
                }.launchIn(msgsScope!!)
            }
        }.launchIn(chatScope)
    }

    private fun getOrCreateChatUI(user: User): ChatUI = synchronized(this) { mappedChats.firstOrNull { chat -> chat.participants.value.others.all { it.userId == user.userId }} ?: createChatUI(chatBox.create(user)) }

    private fun getOrCreateChatUI(chat: Chat): ChatUI = synchronized(this) { mappedChats.firstOrNull { it.id == chat.id } ?: createChatUI(chat) }

    private fun createChatUI(chat: Chat): ChatUI = ChatUI(chat, chatActivityClazz = chatActivityClazz, chatCustomNotificationActivityClazz = chatCustomNotificationActivityClazz).apply { mappedChats = mappedChats + this }
}


package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.utils.extensions.mapToStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

class ChatBoxUI(
    private val chatBox: ChatBox,
    private val userId: String,
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivity: Class<*>? = null
//    private val logger: PriorityLogger? = null
) : ChatBox by chatBox {

    private var chatScope: CoroutineScope? = null

    var withUI: Boolean = true

    override val chats: StateFlow<List<ChatUI>> = chatBox.chats.mapToStateFlow(MainScope()) {
        it.map {
            ChatUI(it, chatActivityClazz = chatActivityClazz, chatNotificationActivityClazz = chatCustomNotificationActivity)
        }
    }

    private var lastMessagePerChat: HashMap<String, String> = hashMapOf()

    override fun connect() {
        chatBox.connect()
        chatBox.fetch(10)
        if (chatScope?.isActive == true) return
        listenToMessages()
    }

    override fun disconnect(clearSavedData: Boolean) {
        chatBox.disconnect(clearSavedData)
        chatScope?.cancel()
    }

    private fun listenToMessages() {
        chatScope = MainScope()
        var msgsScope: CoroutineScope? = null
        chats.onEach { chats ->
            msgsScope?.cancel()
            msgsScope = CoroutineScope(SupervisorJob(chatScope!!.coroutineContext[Job]))
            chats.forEach { chat ->
                chat.messages.onEach messagesUI@{
                    if (!withUI) return@messagesUI
                    val lastMessage = it.other.firstOrNull { it.state.value is Message.State.Received }
                    if (lastMessage == null || lastMessagePerChat[chat.id] == lastMessage.id) return@messagesUI
                    lastMessagePerChat[chat.id] = lastMessage.id
                    it.showUnreadMsgs(chat.id, userId)
                }.launchIn(msgsScope!!)
            }
        }.launchIn(chatScope!!)
    }

    /**
     * Show the chat ui
     * @param context context to bind the chat ui
     * @param chat The chat object that should be shown.
     */
    fun show(context: Context, chat: ChatUI) =
        UIProvider.showChat(context, chatActivityClazz, chat.id)

    override fun create(user: User) = ChatUI(
        chatBox.create(user),
        chatActivityClazz = chatActivityClazz,
        chatNotificationActivityClazz = chatCustomNotificationActivity
    )

    /**
     * Given a user, open a chat ui.
     * @param context launching context of the chat ui
     * @param user The user with whom you want to chat.
     */
    fun chat(context: Context, user: User): ChatUI = create(user).apply { show(context, this) }
}
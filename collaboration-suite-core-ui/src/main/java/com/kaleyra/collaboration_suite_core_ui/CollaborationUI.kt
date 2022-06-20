/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite.Collaboration.Credentials
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.Message.Content.Text
import com.kaleyra.collaboration_suite.chatbox.Messages
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Call.PreferredType
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.CreationOptions
import com.kaleyra.collaboration_suite.utils.extensions.mapToStateFlow
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ChangeVolume
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ShowParticipants
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.SwitchCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleMicrophone
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI.usersDescription
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceBinder
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationMessage
import com.kaleyra.collaboration_suite_core_ui.notification.CustomChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.cached
import com.kaleyra.collaboration_suite_utils.getValue
import com.kaleyra.collaboration_suite_utils.logging.PriorityLogger
import com.kaleyra.collaboration_suite_utils.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

/**
 * Collaboration UI
 *
 * This object allows the usage of a Collaboration UI
 */
object CollaborationUI {

    private var collaboration: Collaboration? = null

    private var chatNotificationActivityClazz: Class<*>? = null

    private lateinit var chatActivityClazz: Class<*>
    private lateinit var callActivityClazz: Class<*>

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            startCollaborationService(wasPhoneBoxConnected, wasChatBoxConnected)
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            wasPhoneBoxConnected = phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
            wasChatBoxConnected = chatBox.state.value.let { it !is ChatBox.State.Disconnected && it !is ChatBox.State.Disconnecting }
        }
    }

    /**
     * Users description to be used for the UI
     */
    var usersDescription: UsersDescription = UsersDescription()

    private var _phoneBox: PhoneBoxUI? by cached { PhoneBoxUI(collaboration!!.phoneBox, callActivityClazz, collaboration!!.configuration.logger) }
    private var _chatBox: ChatBoxUI? by cached {
        ChatBoxUI(
            collaboration!!.chatBox, collaboration!!.configuration.userId, chatActivityClazz, chatNotificationActivityClazz
//        collaboration!!.configuration.logger
        )
    }

    /**
     * Phone box
     */
    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
            return _phoneBox!!
        }

    /**
     * Chat box
     */
    val chatBox: ChatBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the chatBox" }
            return _chatBox!!
        }

    /**
     * Set up
     *
     * @param T activity of type [CallActivity] to be used for the UI
     * @param credentials to use when Collaboration tools need to be connected
     * @param configuration representing a set of info necessary to instantiate the communication
     * @param callActivityClazz class of the activity
     * @return
     */
    fun setUp(
        credentials: Credentials,
        configuration: Configuration,
        callActivityClazz: Class<*>,
        chatActivityClazz: Class<*>,
        chatNotificationActivityClazz: Class<*>? = null
    ): Boolean {
        if (collaboration != null) return false
        Collaboration.create(credentials, configuration).apply { collaboration = this }
        this.chatActivityClazz = chatActivityClazz
        this.callActivityClazz = callActivityClazz
        this.chatNotificationActivityClazz = chatNotificationActivityClazz
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        return true
    }

    /**
     * Connect
     */
    fun connect() {
        collaboration ?: return
        startCollaborationService(true, true)
    }

    /**
     * Disconnect
     */
    fun disconnect() = disconnectCollaboration(false)

    private fun disconnectCollaboration(clearSavedData: Boolean = false) {
        collaboration ?: return
        phoneBox.disconnect()
        chatBox.disconnect(clearSavedData)
        stopCollaborationService()
    }

    /**
     * Dispose the collaboration UI and optionally clear saved data.
     * @param clearSavedData If true, the saved data on DB and SharedPrefs will be cleared.
     */
    fun dispose(clearSavedData: Boolean = true) {
        collaboration ?: return
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        disconnectCollaboration(clearSavedData)
        collaboration = null
        _phoneBox = null
        _chatBox = null
    }

    private fun startCollaborationService(startPhoneBox: Boolean, startChatBox: Boolean) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                if (startPhoneBox) phoneBox.connect()
                if (startChatBox) chatBox.connect()
            }

            override fun onServiceDisconnected(componentName: ComponentName) = Unit
        }

        with(ContextRetainer.context) {
            val intent = Intent(this, CollaborationService::class.java)
            startService(intent)
            bindService(intent, serviceConnection, 0)
        }
    }

    private fun stopCollaborationService() = with(ContextRetainer.context) {
        stopService(Intent(this, CollaborationService::class.java))
    }
}

/**
 * Phone box UI
 *
 * @param phoneBox delegated property
 */
class PhoneBoxUI(
    private val phoneBox: PhoneBox,
    private val callActivityClazz: Class<*>,
    private val logger: PriorityLogger? = null,
) : PhoneBox by phoneBox {

    private var callScope: CoroutineScope? = null

    var withUI = true

    override val call: SharedFlow<CallUI> = phoneBox.call.map { CallUI(it) }.shareIn(MainScope(), SharingStarted.Eagerly, replay = 1)

    override val callHistory: SharedFlow<List<CallUI>> = phoneBox.callHistory.map { it.map { CallUI(it) } }.shareIn(MainScope(), SharingStarted.Eagerly, replay = 1)

    override fun connect() {
        phoneBox.connect()
        disableAudioRouting(logger)
        if (callScope?.isActive == true) return
        listenToCalls()
    }

    override fun disconnect() {
        phoneBox.disconnect()
        disableAudioRouting(logger)
        callScope?.cancel()
    }

    private fun listenToCalls() {
        callScope = MainScope()
        call.onEach {
            if (it.state is Call.State.Disconnected.Ended || !withUI) return@onEach
            CollaborationUI.phoneBox.enableAudioRouting(withCallSounds = true, logger = logger, coroutineScope = callScope!!)
            show(it)
        }.launchIn(callScope!!)
    }

    /**
     * Call
     *
     * @param users to be called
     * @param options creation options
     */
    fun call(users: List<User>, options: (CreationOptions.() -> Unit)? = null): CallUI = create(users, options).apply { connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    fun join(url: String): CallUI = create(url).apply { connect() }

    override fun create(url: String) = CallUI(phoneBox.create(url))

    override fun create(users: List<User>, conf: (CreationOptions.() -> Unit)?) = CallUI(phoneBox.create(users, conf))

    /**
     * Show the call ui
     * @param call The call object that should be shown.
     */
    fun show(call: CallUI) = bindCollaborationService(call, usersDescription, callActivityClazz)

    private fun bindCollaborationService(
        call: CallUI,
        usersDescription: UsersDescription,
        callActivityClazz: Class<*>
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                service.bindCall(call, usersDescription, callActivityClazz)
                if (!canShowCallActivity(service, call)) return
                UIProvider.showCall(callActivityClazz)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        with(ContextRetainer.context) {
            val intent = Intent(this, CollaborationService::class.java)
            bindService(intent, serviceConnection, 0)
        }
    }

    private fun canShowCallActivity(context: Context, call: Call): Boolean {
        val participants = call.participants.value
        val creator = participants.creator()
        val isOutgoing = creator == participants.me
        val isLink = creator == null
        return AppLifecycle.isInForeground.value &&
                (!context.isDND() || (context.isDND() && isOutgoing)) &&
                (!context.isSilent() || (context.isSilent() && (isOutgoing || isLink)))
    }
}

private fun Call.getDefaultActions() = mutableSetOf<Action>().apply {
    if (extras.preferredType.hasAudio()) add(ToggleMicrophone)
    if (extras.preferredType.hasVideo()) {
        add(ToggleCamera)
        add(SwitchCamera)
    }
    add(ChangeVolume)
    add(ShowParticipants)
}

class CallUI(
    call: Call,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(call.getDefaultActions())
) : Call by call {

    @Keep
    sealed class Action : Parcelable {

        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    ToggleMicrophone,
                    ToggleCamera,
                    SwitchCamera,
                    ChangeZoom,
                    ToggleFlashlight,
                    OpenChat.ViewOnly,
                    ShowParticipants,
                    OpenWhiteboard.ViewOnly
                )
            }
        }

        @Parcelize
        object ChangeVolume : Action()

        @Parcelize
        object ToggleCamera : Action()

        @Parcelize
        object ToggleMicrophone : Action()

        @Parcelize
        object SwitchCamera : Action()

        @Parcelize
        object ChangeZoom : Action()

        @Parcelize
        object ToggleFlashlight : Action()

        @Parcelize
        object ShowParticipants : Action()

        sealed class OpenChat : Action() {
            @Parcelize
            object ViewOnly : OpenChat()
        }

        sealed class OpenWhiteboard : Action() {
            @Parcelize
            object ViewOnly : OpenWhiteboard()
        }
    }
}

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

class ChatUI(
    chat: Chat,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(setOf()),
    private val chatActivityClazz: Class<*>,
    private val chatNotificationActivityClazz: Class<*>? = null
) : Chat by chat {

    override val messages: StateFlow<MessagesUI> = chat.messages.mapToStateFlow(MainScope()) {
        MessagesUI(it, chatActivityClazz, chatNotificationActivityClazz)
    }

    @Keep
    sealed class Action : Parcelable {
        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    CreateCall(preferredType = PreferredType(video = Call.Video.Disabled)),
                    CreateCall()
                )
            }
        }

        @Parcelize
        data class CreateCall(val preferredType: PreferredType = PreferredType()) : Action()
    }
}

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
            usersDescription.name(listOf(loggedUserId)),
            usersDescription.image(listOf(loggedUserId)),
            chatId,
            messages,
            chatActivityClazz
        )
        NotificationManager.notify(chatId.hashCode(), notification)
    }

    private suspend fun OtherMessage.toChatNotificationMessage() = ChatNotificationMessage(
        creator.userId,
        usersDescription.name(listOf(creator.userId)),
        usersDescription.image(listOf(creator.userId)),
        (content as? Text)?.message ?: "",
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
            usersDescription.name(listOf(loggedUserId)),
            usersDescription.image(listOf(loggedUserId)),
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


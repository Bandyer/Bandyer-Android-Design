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
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Call.PreferredType
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.CreationOptions
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.State.Connecting
import com.kaleyra.collaboration_suite.utils.extensions.mapToStateFlow
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ChangeVolume
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ShowParticipants
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.SwitchCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleMicrophone
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI.usersDescription
import com.kaleyra.collaboration_suite_core_ui.call.CallActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceBinder
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.parcelize.Parcelize

/**
 * Collaboration UI
 *
 * This object allows the usage of a Collaboration UI
 */
object CollaborationUI {

    private var collaboration: Collaboration? = null

    private var chatActivityClazz: Class<*>? = null
    private var callActivityClazz: Class<*>? = null

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            if (wasPhoneBoxConnected) phoneBox.connect()
            if (wasChatBoxConnected) chatBox.connect()
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            wasPhoneBoxConnected =
                phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
            wasChatBoxConnected =
                chatBox.state.value.let { it !is ChatBox.State.Disconnected && it !is ChatBox.State.Disconnecting }
        }
    }

    /**
     * Users description to be used for the UI
     */
    var usersDescription: UsersDescription? = null

    /**
     * Phone box
     */
    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null && callActivityClazz != null) { "setUp the CollaborationUI to use the phoneBox" }
            return PhoneBoxUI(collaboration!!.phoneBox, callActivityClazz!!)
        }

    /**
     * Chat box
     */
    val chatBox: ChatBoxUI
        get() {
            require(collaboration != null && chatActivityClazz != null) { "setUp the CollaborationUI to use the chatBox" }
            return ChatBoxUI(collaboration!!.chatBox, chatActivityClazz!!)
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
    fun <T : CallActivity, S : ChatActivity> setUp(
        credentials: Credentials,
        configuration: Configuration,
        callActivityClazz: Class<T>,
        chatActivityClazz: Class<S>? = null,
        chatNotificationActivityClazz: Class<*>? = null
    ): Boolean {
        if (collaboration != null) return false
        Collaboration.create(credentials, configuration).apply { collaboration = this }
        this.chatActivityClazz = chatActivityClazz
        this.callActivityClazz = callActivityClazz
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        phoneBox.state
            .filter { it is Connecting }
            .onEach { startCollaborationService(callActivityClazz, chatNotificationActivityClazz) }
            .launchIn(MainScope())

        phoneBox.enableAudioRouting(logger = collaboration?.configuration?.logger)
        return true
    }

    /**
     * Connect
     */
    fun connect() {
        phoneBox.connect()
        chatBox.connect()
    }

    /**
     * Disconnect
     */
    fun disconnect() {
        phoneBox.disconnect()
        chatBox.disconnect()
    }

    /**
     * Dispose the collaboration UI
     */
    fun dispose() {
        if (collaboration == null) return
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        wasPhoneBoxConnected = false
        stopCollaborationService()
        disconnect()
        phoneBox.disableAudioRouting(logger = collaboration?.configuration?.logger)
        collaboration = null
    }

    private fun <T : CallActivity> startCollaborationService(
        activityClazz: Class<T>,
        chatNotificationActivityClazz: Class<*>?
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                // service.bindPhoneBox(phoneBox, usersDescription, activityClazz)

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
class PhoneBoxUI(private val phoneBox: PhoneBox, private val callActivityClazz: Class<*>) : PhoneBox by phoneBox {

    override val call: SharedFlow<CallUI> = phoneBox.call.map { CallUI(it) }.shareIn(MainScope(), SharingStarted.Eagerly)

    override val callHistory: SharedFlow<List<CallUI>> = phoneBox.callHistory.map { it.map { CallUI(it) } }.shareIn(MainScope(), SharingStarted.Eagerly)

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

    fun show(call: CallUI) = bindCollaborationService(call, usersDescription, callActivityClazz)

    private fun bindCollaborationService(
        call: CallUI,
        usersDescription: UsersDescription?,
        callActivityClazz: Class<*>
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                service.bindCall(this@PhoneBoxUI, call, usersDescription, callActivityClazz)
                if (!service.canShowCallActivity(call)) return
                UIProvider.showCall(callActivityClazz)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        with(ContextRetainer.context) {
            val intent = Intent(this, CollaborationService::class.java)
            bindService(intent, serviceConnection, 0)
        }
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

class CallUI(call: Call, val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(call.getDefaultActions())) : Call by call {

    @Keep
    sealed class Action : Parcelable {

        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy { setOf(ToggleMicrophone, ToggleCamera, SwitchCamera, ChangeZoom, ToggleFlashlight, OpenChat.ViewOnly, ShowParticipants, OpenWhiteboard.ViewOnly) }
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

class ChatBoxUI(private val chatBox: ChatBox, private val chatActivityClazz: Class<*>) : ChatBox by chatBox {

    override val chats: StateFlow<List<ChatUI>> = chatBox.chats.mapToStateFlow(MainScope()) { it.map { ChatUI(it) } }

    fun show(chat: ChatUI) = bindCollaborationService(chat, usersDescription, chatActivityClazz)

    override fun create(users: List<User>) = ChatUI(chatBox.create(users))

//    suspend fun create(list: List<ChatUser>): ChatChannel =
//        createChannel(list).also { bindCollaborationService(it, usersDescription, chatActivityClazz) }

//    fun sendMessage(channel: ChatChannel, message: String) {
//        channel.sendTextMessage(message)
//        bindCollaborationService(channel, usersDescription, chatActivityClazz)
//    }

    private fun bindCollaborationService(
        chat: Chat,
        usersDescription: UsersDescription?,
        chatActivityClazz: Class<*>
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                service.bindChatChannel(chat, usersDescription, chatActivityClazz)
                // if option show notification
                UIProvider.showChat(chatActivityClazz)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        with(ContextRetainer.context) {
            val intent = Intent(this, CollaborationService::class.java)
            bindService(intent, serviceConnection, 0)
        }
    }
}

class ChatUI(chat: Chat, val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(setOf())) : Chat by chat {

    @Keep
    sealed class Action : Parcelable {
        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all = setOf(CreateCall(preferredType = PreferredType(video = Call.Video.Disabled)), CreateCall())
        }

        @Parcelize
        data class CreateCall(val preferredType: PreferredType = PreferredType()) : Action()
    }
}


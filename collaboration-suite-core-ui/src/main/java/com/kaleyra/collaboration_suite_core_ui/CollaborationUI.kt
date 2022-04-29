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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bandyer.android_chat_sdk.ChatClientInstance
import com.bandyer.android_chat_sdk.api.ChatChannel
import com.bandyer.android_chat_sdk.model.ChatUser
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite.Collaboration.Credentials
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.CreationOptions
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.State.Connecting
import com.kaleyra.collaboration_suite_core_ui.call.CallActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.mockChatClient
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceBinder
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Collaboration UI
 *
 * This object allows the usage of a Collaboration UI
 */
object CollaborationUI {

    private var collaboration: Collaboration? = null
    private var chatClient: ChatClientInstance? = null
    private var chatActivityClazz: Class<*>? = null

    private var wasPhoneBoxConnected = false

    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            if (wasPhoneBoxConnected) phoneBox.connect()
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            wasPhoneBoxConnected =
                phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
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
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
            return PhoneBoxUI(collaboration!!.phoneBox)
        }

    /**
     * Phone box
     */
    val chatClientUI: ChatClientUI
        get() {
            require(chatClient != null) { "setUp the CollaborationUI to use the chat" }
            require(chatActivityClazz != null) { "setUp the CollaborationUI to use the chat" }
            return ChatClientUI(chatClient!!, usersDescription, chatActivityClazz!!)
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
        chatActivityClazz: Class<S>? = null
    ): Boolean {
        if (collaboration != null) return false
        Collaboration.create(credentials, configuration).apply { collaboration = this }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        phoneBox.state
            .filter { it is Connecting }
            .onEach { startCollaborationService(callActivityClazz) }
            .launchIn(MainScope())

        this.chatClient = mockChatClient
        this.chatActivityClazz = chatActivityClazz
        return true
    }

    /**
     * Dispose the collaboration UI
     */
    fun dispose() {
        if (collaboration == null) return
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        wasPhoneBoxConnected = false
        stopCollaborationService()
        phoneBox.disconnect()
        collaboration = null
    }

    private fun <T : CallActivity> startCollaborationService(activityClazz: Class<T>) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                service.bindPhoneBox(phoneBox, usersDescription, activityClazz)
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
class PhoneBoxUI(phoneBox: PhoneBox) : PhoneBox by phoneBox {

    /**
     * Call
     *
     * @param users to be called
     * @param options creation options
     */
    fun call(users: List<User>, options: (CreationOptions.() -> Unit)? = null) =
        create(users, options).apply { connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    fun join(url: String) = create(url).apply { connect() }
}

class ChatClientUI(
    chatClient: ChatClientInstance,
    private val usersDescription: UsersDescription? = null,
    private val chatActivityClazz: Class<*>
) : ChatClientInstance by chatClient {
    fun show(channel: ChatChannel) {
        bindCollaborationService(channel, usersDescription, chatActivityClazz)
    }

    suspend fun create(list: List<ChatUser>): ChatChannel =
        chatChannels.create(list).also { bindCollaborationService(it, usersDescription, chatActivityClazz) }

    fun sendMessage(channel: ChatChannel, message: String) {
        channel.chatMessages.sendTextMessage(message)
        bindCollaborationService(channel, usersDescription, chatActivityClazz)
    }

    private fun bindCollaborationService(
        chatChannel: ChatChannel,
        usersDescription: UsersDescription?,
        chatActivityClazz: Class<*>
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as BoundServiceBinder).getService<CollaborationService>()
                service.bindChatChannel(chatChannel, usersDescription)
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
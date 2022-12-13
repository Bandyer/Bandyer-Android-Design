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

import android.net.Uri
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.cached
import com.kaleyra.collaboration_suite_utils.getValue
import com.kaleyra.collaboration_suite_utils.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

/**
 * Collaboration UI
 *
 * This object allows the usage of a Collaboration UI
 */
object CollaborationUI {

    /**
     * Collaboration
     */
    private var collaboration: Collaboration? = null

    private var mainScope: CoroutineScope? = null

    private lateinit var callActivityClazz: Class<*>
    private lateinit var chatActivityClazz: Class<*>
    private var chatNotificationActivityClazz: Class<*>? = null

    private var collaborationUIConnector: CollaborationUIConnector? = null

    private var _phoneBox: PhoneBoxUI? by cached { PhoneBoxUI(collaboration!!.phoneBox, callActivityClazz, collaboration!!.configuration.logger) }
    private var _chatBox: ChatBoxUI? by cached { ChatBoxUI(collaboration!!.chatBox, chatActivityClazz, chatNotificationActivityClazz) }

    /**
     * Users description to be used for the UI
     */
    var usersDescription: UsersDescription = UsersDescription()

    /**
     * Phone box
     */
    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "configure the CollaborationUI to use the phoneBox" }
            return _phoneBox!!
        }

    /**
     * Is configured
     */
    val isConfigured
        get() = collaboration != null

    /**
     * Chat box
     */
    val chatBox: ChatBoxUI
        get() {
            require(collaboration != null) { "configure the CollaborationUI to use the chatBox" }
            return _chatBox!!
        }

    /**
     * Configure
     *
     * @param configuration representing a set of info necessary to instantiate the communication
     * @param callActivityClazz class of the call activity
     * @param chatActivityClazz class of the chat activity
     * @param chatNotificationActivityClazz class of the chat notification fullscreen activity
     * @return
     */
    fun configure(
        configuration: Configuration,
        callActivityClazz: Class<*>,
        chatActivityClazz: Class<*>,
        chatNotificationActivityClazz: Class<*>? = null
    ): Boolean {
        if (collaboration != null) return false
        Collaboration.create(configuration).apply {
            collaboration = this
        }
        this.chatActivityClazz = chatActivityClazz
        this.callActivityClazz = callActivityClazz
        this.chatNotificationActivityClazz = chatNotificationActivityClazz
        mainScope = MainScope()
        collaborationUIConnector = CollaborationUIConnector(collaboration!!, mainScope!!)
        return true
    }

    /**
     * Connect
     */
    fun connect(session: Collaboration.Session) {
        if (collaboration?.session != null && collaboration?.session?.userId != session.userId) disconnect(true)
        collaborationUIConnector?.connect(session)
    }

    /**
     * Disconnect
     * @param clearSavedData If true, the saved data on DB and SharedPrefs will be cleared.
     */
    fun disconnect(clearSavedData: Boolean = false) {
        collaborationUIConnector?.disconnect(clearSavedData)
    }

    /**
     * Dispose the collaboration UI and optionally clear saved data.
     */
    fun dispose() {
        collaboration ?: return
        mainScope?.cancel()
        disconnect(true)
        collaboration = null
        _phoneBox = null
        _chatBox = null
        mainScope = null
    }
}

internal fun CollaborationUI.onCallReady(scope: CoroutineScope, block: (call: CallUI) -> Unit) {
    phoneBox.call
        .take(1)
        .onEach { block.invoke(it) }
        .launchIn(scope)
}


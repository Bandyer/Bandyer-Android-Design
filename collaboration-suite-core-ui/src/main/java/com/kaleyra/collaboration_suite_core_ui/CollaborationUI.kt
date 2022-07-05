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

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite.Collaboration.Credentials
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceBinder
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.gotToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.ContextRetainer.Companion.context
import com.kaleyra.collaboration_suite_utils.cached
import com.kaleyra.collaboration_suite_utils.getValue
import com.kaleyra.collaboration_suite_utils.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

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
    private var _chatBox: ChatBoxUI? by cached {
        ChatBoxUI(
            collaboration!!.chatBox, collaboration!!.configuration.userId, chatActivityClazz, chatNotificationActivityClazz
//        collaboration!!.configuration.logger
        )
    }

    /**
     * Users description to be used for the UI
     */
    var usersDescription: UsersDescription = UsersDescription()

    /**
     * Phone box
     */
    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
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
            require(collaboration != null) { "setUp the CollaborationUI to use the chatBox" }
            return _chatBox!!
        }

    /**
     * Set up
     *
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
        Collaboration.create(credentials, configuration).apply {
            collaboration = this
        }
        this.chatActivityClazz = chatActivityClazz
        this.callActivityClazz = callActivityClazz
        this.chatNotificationActivityClazz = chatNotificationActivityClazz
        mainScope = MainScope()
        collaborationUIConnector = CollaborationUIConnector(this, mainScope!!)
        return true
    }

    /**
     * Connect
     */
    fun connect() {
        collaborationUIConnector?.connect()
    }

    /**
     * Disconnect
     */
    fun disconnect() {
        collaborationUIConnector?.disconnect()
    }

    /**
     * Dispose the collaboration UI and optionally clear saved data.
     * @param clearSavedData If true, the saved data on DB and SharedPrefs will be cleared.
     */
    fun dispose(clearSavedData: Boolean = true) {
        collaboration ?: return
        mainScope?.cancel()
        collaborationUIConnector?.dispose(clearSavedData)
        collaboration = null
        _phoneBox = null
        _chatBox = null
        mainScope = null
    }
}


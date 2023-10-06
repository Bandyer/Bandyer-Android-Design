/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.model.UserDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.TermsAndConditionsRequester
import com.kaleyra.video_networking.connector.AccessTokenProvider
import com.kaleyra.video_networking.connector.ConnectedUser
import com.kaleyra.video_networking.connector.Connector
import com.kaleyra.video_networking.connector.Connector.State
import com.kaleyra.video_networking.connector.Connector.Synchronization
import com.kaleyra.video_utils.cached
import com.kaleyra.video_utils.getValue
import com.kaleyra.video_utils.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * KaleyraVideo
 *
 * This object allows the usage of a KaleyraVideo
 */
object KaleyraVideo : Connector {

    /**
     * Collaboration
     */
    @get:Synchronized @set:Synchronized
    internal var collaboration: Collaboration? = null

    private val serialScope by lazy { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }

    private var mainScope: CoroutineScope? = null

    private lateinit var callActivityClazz: Class<*>
    private lateinit var chatActivityClazz: Class<*>
    private var termsAndConditionsActivityClazz: Class<*>? = null
    private var chatNotificationActivityClazz: Class<*>? = null

    private var collaborationUIConnector: CollaborationUIConnector? = null
    private var termsAndConditionsRequester: TermsAndConditionsRequester? = null

    private var _conference: ConferenceUI? by cached { ConferenceUI(collaboration!!.conference, callActivityClazz, collaboration!!.configuration.logger) }
    private var _conversation: ConversationUI? by cached { ConversationUI(collaboration!!.conversation, chatActivityClazz, chatNotificationActivityClazz) }

    /**
     * Users description to be used for the UI
     */
    @get:Synchronized @set:Synchronized
    var userDetailsProvider: UserDetailsProvider? = null

    /**
     * Conference
     */
    val conference: ConferenceUI
        get() {
            require(collaboration != null) { "configure the CollaborationUI to use the conference" }
            return _conference!!
        }

    /**
     * Is configured
     */
    val isConfigured
        get() = collaboration != null

    /**
     * Conversation
     */
    val conversation: ConversationUI
        get() {
            require(collaboration != null) { "configure the KaleyraVideo to use the conversation" }
            return _conversation!!
        }

    val companyName: SharedFlow<String> by lazy { collaboration?.company?.name ?: MutableSharedFlow() }

    val companyTheme: SharedFlow<Company.Theme> by lazy { collaboration?.company?.theme ?: MutableSharedFlow() }

    var theme: CompanyUI.Theme? = null

    /**
     * Configure
     *
     * @param configuration representing a set of info necessary to instantiate the communication
     * @param callActivityClazz class of the call activity
     * @param chatActivityClazz class of the chat activity
     * @param termsAndConditionsActivityClazz class of the terms and conditions activity
     * @param chatNotificationActivityClazz class of the chat notification fullscreen activity
     * @return
     */
    fun configure(
        configuration: Configuration,
        callActivityClazz: Class<*>,
        chatActivityClazz: Class<*>,
        termsAndConditionsActivityClazz: Class<*>? = null,
        chatNotificationActivityClazz: Class<*>? = null
    ): Boolean {
        if (isConfigured) return false
        Collaboration.create(configuration).apply {
            collaboration = this
        }
        this.chatActivityClazz = chatActivityClazz
        this.callActivityClazz = callActivityClazz
        this.termsAndConditionsActivityClazz = termsAndConditionsActivityClazz
        this.chatNotificationActivityClazz = chatNotificationActivityClazz
        mainScope = MainScope()
        collaborationUIConnector = CollaborationUIConnector(collaboration!!, mainScope!!)
        termsAndConditionsActivityClazz?.also {
            termsAndConditionsRequester = TermsAndConditionsRequester(it)
        }
        return true
    }

    override val state: StateFlow<State>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the state" }
            return collaboration!!.state
        }

    override val synchronization: StateFlow<Synchronization>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the synchronization" }
            return collaboration!!.synchronization
        }

    override val connectedUser: StateFlow<ConnectedUser?>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the connectedUser" }
            return collaboration!!.connectedUser
        }

    /**
     * Connect
     */
    override fun connect(userId: String, accessTokenProvider: AccessTokenProvider): Deferred<ConnectedUser> = CompletableDeferred<ConnectedUser>().apply {
        serialScope.launch {
            val connect = collaborationUIConnector?.connect(userId, accessTokenProvider) ?: return@launch
            connect.invokeOnCompletion {
                if(it != null) completeExceptionally(it)
                else complete(connect.getCompleted())
            }
            termsAndConditionsRequester?.setUp(state, ::disconnect)
        }
    }

    override fun connect(accessLink: String): Deferred<ConnectedUser> = CompletableDeferred<ConnectedUser>().apply {
        serialScope.launch {
            val connect = collaborationUIConnector?.connect(accessLink) ?: return@launch
            connect.invokeOnCompletion {
                if(it != null) completeExceptionally(it)
                else complete(connect.getCompleted())
            }
            termsAndConditionsRequester?.setUp(state, ::disconnect)
        }
    }

    override fun disconnect(clearSavedData: Boolean) {
        serialScope.launch {
            collaborationUIConnector?.disconnect(clearSavedData)
            termsAndConditionsRequester?.dispose()
        }
    }

    /**
     * Dispose the collaboration UI and optionally clear saved data.
     */
    fun reset() {
        serialScope.launch {
            collaboration ?: return@launch
            mainScope?.cancel()
            collaborationUIConnector?.disconnect(true)
            termsAndConditionsRequester?.dispose()
            collaboration = null
            _conference = null
            _conversation = null
            mainScope = null
        }
    }
}

internal fun KaleyraVideo.onCallReady(scope: CoroutineScope, block: (call: CallUI) -> Unit) {
    conference.call
        .take(1)
        .onEach { block.invoke(it) }
        .launchIn(scope)
}


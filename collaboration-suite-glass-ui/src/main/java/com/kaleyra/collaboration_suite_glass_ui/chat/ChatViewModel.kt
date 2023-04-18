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

package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.ChatDelegate
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.DeviceStatusObserver
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ChatViewModel : ViewModel() {

    //////////////////////////
    // DeviceStatusObserver //
    //////////////////////////
    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }

    //////////////////
    // ChatDelegate //
    //////////////////
    var chatDelegate: ChatDelegate? = null

    private val _chat: MutableSharedFlow<ChatUI> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val chat: SharedFlow<ChatUI> = _chat.asSharedFlow()

    val actions: StateFlow<Set<ChatUI.Action>> = chat.flatMapLatest { it.actions }.stateIn(viewModelScope, SharingStarted.Eagerly, setOf())

    val usersDescription: UsersDescription
        get() = chatDelegate?.usersDescription ?: UsersDescription()

    val participants: SharedFlow<ChatParticipants> =
        chat.flatMapLatest { it.participants }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox ?: return null
        val chat = chatBox.activeChats.replayCache.firstOrNull()?.firstOrNull { it.participants.value.others.all { it.userId == userId } } ?: chatBox.create(listOf(userId)).getOrNull() ?: return null
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }

    /////////////
    // ChatBox //
    /////////////
    private var chatBoxScope: CoroutineScope? = null
    var chatBox: ChatBoxUI? = null
        set(value) {
            chatBoxScope?.cancel()
            chatBoxScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
            value?.state?.onEach { _chatBoxState.value = it }?.launchIn(chatBoxScope!!)
            field = value
        }

    private val _chatBoxState: MutableStateFlow<ChatBox.State> =
        MutableStateFlow(ChatBox.State.Disconnected)
    val chatBoxState: StateFlow<ChatBox.State> = _chatBoxState.asStateFlow()

    /////////////
    // PhoneBox //
    /////////////
    private var phoneBoxScope: CoroutineScope? = null
    var phoneBox: PhoneBoxUI? = null
        set(value) {
            phoneBoxScope?.cancel()
            phoneBoxScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
            value?.call?.onEach { _call.emit(it) }?.launchIn(phoneBoxScope!!)
            field = value
        }

    private val _call: MutableSharedFlow<CallUI> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val call: SharedFlow<CallUI> = _call.asSharedFlow()
}
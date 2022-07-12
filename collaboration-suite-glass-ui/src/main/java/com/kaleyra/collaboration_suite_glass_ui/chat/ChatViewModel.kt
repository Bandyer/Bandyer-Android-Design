package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
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

    fun setChat(userId: String): ChatUI? {
        chatBox ?: chatDelegate ?: return null
        val chat = chatBox!!.create(object : User { override val userId = userId })
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
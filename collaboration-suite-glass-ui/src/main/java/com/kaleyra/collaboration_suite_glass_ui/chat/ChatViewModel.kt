package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
internal class ChatViewModelFactory(
    private val chats: StateFlow<List<Chat>>,
    private val batteryObserver: BatteryObserver,
    private val wiFiObserver: WiFiObserver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(chats, batteryObserver, wiFiObserver) as T
}

class ChatViewModel(
    private val chats: StateFlow<List<Chat>>,
    batteryObserver: BatteryObserver,
    wiFiObserver: WiFiObserver,
) : ViewModel() {

    private val _chat: MutableSharedFlow<Chat> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val chat: SharedFlow<Chat> = _chat.asSharedFlow()

    val battery: SharedFlow<BatteryInfo> = batteryObserver.observe()

    val wifi: SharedFlow<WiFiInfo> = wiFiObserver.observe()

    fun setChat(chatId: String) = viewModelScope.launch {
        val chat = chats.value.first { it.id == chatId }
        _chat.emit(chat)
    }
}
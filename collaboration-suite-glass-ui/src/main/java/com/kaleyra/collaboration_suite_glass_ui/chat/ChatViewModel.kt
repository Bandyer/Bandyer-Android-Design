package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chats: StateFlow<List<Chat>> = CollaborationUI.chatBox.chats

    private val _chat: MutableSharedFlow<Chat> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val chat: SharedFlow<Chat> = _chat.asSharedFlow()

    val battery: SharedFlow<BatteryInfo> = MutableSharedFlow(replay = 1)
//        deviceStatusDelegate?.battery

    val wifi: SharedFlow<WiFiInfo> = MutableSharedFlow(replay = 1)
//        deviceStatusDelegate?.wifi

    fun setChat(chatId: String) = viewModelScope.launch {
        val chat = chats.value.first { it.id == chatId }
        _chat.emit(chat)
    }
}
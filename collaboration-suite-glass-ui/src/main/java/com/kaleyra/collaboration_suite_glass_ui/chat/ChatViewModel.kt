package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

@Suppress("UNCHECKED_CAST")
internal class ChatViewModelFactory(
    private val chatDelegate: ChatUIDelegate,
    private val deviceStatusDelegate: DeviceStatusDelegate
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(chatDelegate, deviceStatusDelegate) as T
}

class ChatViewModel(
    chatDelegate: ChatUIDelegate,
    deviceStatusDelegate: DeviceStatusDelegate
) : ViewModel() {

    val chat: SharedFlow<Chat> = chatDelegate.chat

    val messages = chat.flatMapLatest { it.messages }.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val usersDescription: UsersDescription = chatDelegate.chatUsersDescription

    val battery: SharedFlow<BatteryInfo> = deviceStatusDelegate.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusDelegate.wifi
}
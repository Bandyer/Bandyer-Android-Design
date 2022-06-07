package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow

@Suppress("UNCHECKED_CAST")
internal class ChatViewModelFactory(
    private val deviceStatusDelegate: DeviceStatusDelegate
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(deviceStatusDelegate) as T
}

class ChatViewModel(
    deviceStatusDelegate: DeviceStatusDelegate
) : ViewModel() {

    fun getChat(chatId: String): Chat =
        CollaborationUI.chatBox.chats.value.first { it.id == chatId }

    val battery: SharedFlow<BatteryInfo> = deviceStatusDelegate.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusDelegate.wifi
}
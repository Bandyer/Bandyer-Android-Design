package com.kaleyra.collaboration_suite_glass_ui.chat

import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.DeviceStatusObserver
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow

class GlassChatViewModel : ChatViewModel() {

    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }
}
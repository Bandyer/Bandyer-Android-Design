package com.bandyer.video_android_core_ui.common

import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow

interface DeviceStatusDelegate {
    val battery: SharedFlow<BatteryInfo>
    val wifi: SharedFlow<WiFiInfo>
}
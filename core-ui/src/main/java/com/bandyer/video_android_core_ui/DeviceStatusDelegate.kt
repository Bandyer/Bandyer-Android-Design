package com.bandyer.video_android_core_ui

import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow

interface DeviceStatusDelegate {
    val battery: SharedFlow<BatteryInfo>
    val wifi: SharedFlow<WiFiInfo>
}
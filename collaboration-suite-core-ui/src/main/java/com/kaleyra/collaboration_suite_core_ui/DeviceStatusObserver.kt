package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.flow.SharedFlow

class DeviceStatusObserver: DeviceStatusDelegate {

    private val batteryObserver = BatteryObserver(ContextRetainer.context)

    private val wiFiObserver = WiFiObserver(ContextRetainer.context)

    override val battery: SharedFlow<BatteryInfo> = batteryObserver.observe()
    override val wifi: SharedFlow<WiFiInfo> = wiFiObserver.observe()

    fun start() {
        batteryObserver.start()
        wiFiObserver.start()
    }

    fun stop() {
        batteryObserver.stop()
        wiFiObserver.stop()
    }
}
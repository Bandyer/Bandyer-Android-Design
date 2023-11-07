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

package com.kaleyra.video_common_ui

import com.kaleyra.video_common_ui.common.DeviceStatusDelegate
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.battery_observer.BatteryInfo
import com.kaleyra.video_utils.battery_observer.BatteryObserver
import com.kaleyra.video_utils.network_observer.WiFiInfo
import com.kaleyra.video_utils.network_observer.WiFiObserver
import kotlinx.coroutines.flow.SharedFlow

/**
 * The device status observer
 */
class DeviceStatusObserver: DeviceStatusDelegate {

    private val batteryObserver = BatteryObserver(ContextRetainer.context)

    private val wiFiObserver = WiFiObserver(ContextRetainer.context)

    /**
     * @suppress
     */
    override val battery: SharedFlow<BatteryInfo> = batteryObserver.observe()

    /**
     * @suppress
     */
    override val wifi: SharedFlow<WiFiInfo> = wiFiObserver.observe()

    /**
     * Start to observe the device battery and wifi statuses
     */
    fun start() {
        batteryObserver.start()
        wiFiObserver.start()
    }

    /**
     * Stop to observe the device status
     */
    fun stop() {
        batteryObserver.stop()
        wiFiObserver.stop()
    }
}
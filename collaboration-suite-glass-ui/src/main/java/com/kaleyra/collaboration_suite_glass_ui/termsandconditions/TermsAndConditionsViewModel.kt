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

package com.kaleyra.collaboration_suite_glass_ui.termsandconditions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.*

internal class TermsAndConditionsViewModel(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TermsAndConditionsViewModel(configure) as T
            }
        }
    }
}
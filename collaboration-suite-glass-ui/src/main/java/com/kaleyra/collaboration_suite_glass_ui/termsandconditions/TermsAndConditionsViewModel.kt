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
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class TermsAndConditionsViewModel(configure: suspend () -> Configuration) : ViewModel() {

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val phoneBox = _configuration.mapSuccess { it.phoneBox }.shareInEagerly(viewModelScope)

    val chatBox = _configuration.mapSuccess { it.chatBoxUI }.shareInEagerly(viewModelScope)

    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    init {
        viewModelScope.launch {
            _configuration.emit(configure())
        }
    }

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }

    private inline fun <T> Flow<Configuration>.mapSuccess(crossinline block: (Configuration.Success) -> T): Flow<T> =
        filterIsInstance<Configuration.Success>().map { block(it) }

    private fun <T> Flow<T>.shareInEagerly(scope: CoroutineScope): SharedFlow<T> =
        this@shareInEagerly.shareIn(scope, SharingStarted.Eagerly, 1)

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TermsAndConditionsViewModel(configure) as T
            }
        }
    }
}

sealed class Configuration {
    data class Success(val phoneBox: PhoneBoxUI, val chatBoxUI: ChatBoxUI, val usersDescription: UsersDescription) : Configuration()
    object Failure : Configuration()
}

suspend fun requestConfiguration(): Configuration {
    if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
    return if (CollaborationUI.isConfigured) Configuration.Success(CollaborationUI.phoneBox, CollaborationUI.chatBox, CollaborationUI.usersDescription)
    else Configuration.Failure
}
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.Company
import com.kaleyra.video.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    sealed class Configuration {
        data class Success(val conference: ConferenceUI, val conversation: ConversationUI, val company: Company, val connectedUser: StateFlow<User?>) : Configuration()
        data object Failure : Configuration()
    }

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareInEagerly(viewModelScope)

    val conference = _configuration.mapSuccess { it.conference }.shareInEagerly(viewModelScope)

    val conversation = _configuration.mapSuccess { it.conversation }.shareInEagerly(viewModelScope)

    val company = _configuration.mapSuccess { it.company }.shareInEagerly(viewModelScope)

    val connectedUser = _configuration.filterIsInstance<Configuration.Success>().flatMapLatest { it.connectedUser }.shareInEagerly(viewModelScope)

    init {
        viewModelScope.launch {
            _configuration.emit(configure())
        }
    }

    private inline fun <T> Flow<Configuration>.mapSuccess(crossinline block: (Configuration.Success) -> T): Flow<T> =
        filterIsInstance<Configuration.Success>().map { block(it) }

    protected fun <T> Flow<T>.shareInEagerly(scope: CoroutineScope): SharedFlow<T> =
        this@shareInEagerly.shareIn(scope, SharingStarted.Eagerly, 1)

    protected fun <T> SharedFlow<T>.getValue(): T? =
        replayCache.firstOrNull()
}

suspend fun requestConfiguration(): CollaborationViewModel.Configuration {
    if (!KaleyraVideo.isConfigured) KaleyraVideoService.get()?.onRequestKaleyraVideoConfigure()
    return if (KaleyraVideo.isConfigured) {
        CollaborationViewModel.Configuration.Success(
            conference = KaleyraVideo.conference,
            conversation = KaleyraVideo.conversation,
            company = KaleyraVideo.collaboration?.company ?: NoOpCompany(),
            connectedUser = KaleyraVideo.connectedUser
        )
    } else CollaborationViewModel.Configuration.Failure
}

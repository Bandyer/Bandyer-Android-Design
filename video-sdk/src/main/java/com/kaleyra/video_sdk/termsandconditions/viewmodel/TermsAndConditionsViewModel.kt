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

package com.kaleyra.video_sdk.termsandconditions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.State
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.termsandconditions.model.TermsAndConditionsUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update

class TermsAndConditionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<TermsAndConditionsUiState>(configure) {

    override fun initialState() = TermsAndConditionsUiState()

    init {
        val conferenceState = conference.flatMapLatest { it.state }
        val conversationState = conversation.flatMapLatest { it.state }
        combine(conferenceState, conversationState) { pbState, cbState ->
            pbState != State.Connecting && cbState != State.Connecting
        }
            .takeWhile { !it }
            .onCompletion { _uiState.update { it.copy(isConnected = true) } }
            .launchIn(viewModelScope)
    }

    fun decline() {
        _uiState.update { it.copy(isDeclined = true) }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TermsAndConditionsViewModel(configure) as T
                }
            }
    }
}
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

package com.kaleyra.video_sdk.call.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_sdk.common.uistate.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

abstract class BaseViewModel<UI_STATE: UiState>(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val _call = MutableSharedFlow<CallUI>(replay = 1)
    protected val call = _call.asSharedFlow()

    protected val _uiState = MutableStateFlow(this.initialState())
    val uiState = _uiState.asStateFlow()

    abstract fun initialState(): UI_STATE

    init {
        viewModelScope.launch {
            val currentCall = conference.flatMapLatest { it.call }.first()
            _call.emit(currentCall)
        }
    }
}
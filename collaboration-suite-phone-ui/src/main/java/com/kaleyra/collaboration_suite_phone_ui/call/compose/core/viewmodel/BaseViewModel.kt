package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
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
            val currentCall = phoneBox.flatMapLatest { it.call }.first()
            _call.emit(currentCall)
        }
    }
}
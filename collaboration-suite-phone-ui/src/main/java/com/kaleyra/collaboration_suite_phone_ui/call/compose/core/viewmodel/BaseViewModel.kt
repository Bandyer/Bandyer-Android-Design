package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

abstract class BaseViewModel<UI_STATE: UiState>(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    protected val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    protected val callUserMessageProvider = CallUserMessagesProvider(call)

    protected val _uiState = MutableStateFlow(this.initialState())
    val uiState = _uiState.asStateFlow()

    abstract fun initialState(): UI_STATE
}
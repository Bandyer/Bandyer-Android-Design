package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel

import androidx.lifecycle.ViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<UI_STATE: UiState> : ViewModel() {

    private val _uiState = MutableStateFlow(this.initialState())
    val uiState = _uiState.asStateFlow()

    abstract fun initialState(): UI_STATE

    protected fun setState(state: UI_STATE) { _uiState.value = state }
}
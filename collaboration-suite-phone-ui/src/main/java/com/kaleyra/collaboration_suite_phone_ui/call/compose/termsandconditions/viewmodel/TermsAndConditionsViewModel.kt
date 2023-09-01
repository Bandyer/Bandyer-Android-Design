package com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.conference.Conference
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.model.TermsAndConditionsUiState
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
        val chatBoxState = chatBox.flatMapLatest { it.state }
        combine(conferenceState, chatBoxState) { pbState, cbState ->
            pbState != Conference.State.Connecting && cbState != ChatBox.State.Connecting
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
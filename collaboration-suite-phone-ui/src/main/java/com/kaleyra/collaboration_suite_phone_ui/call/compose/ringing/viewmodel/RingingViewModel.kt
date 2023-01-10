package com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model.RingingUiState
import kotlinx.coroutines.flow.flatMapLatest

internal class RingingViewModel(configure: suspend () -> Configuration) : BaseViewModel<RingingUiState>(configure) {
    override fun initialState() = RingingUiState()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    init {
//        getMyStream()
//        getCallInfo()
//        isGroupCall()
    }

    fun answer() {
        call.getValue()?.connect()
    }

    fun decline() {
        call.getValue()?.end()
    }

//    fun getMyStream(): StreamUi {
//    }
}
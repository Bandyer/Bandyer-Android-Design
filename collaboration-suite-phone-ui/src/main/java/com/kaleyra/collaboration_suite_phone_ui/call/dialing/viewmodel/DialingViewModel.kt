package com.kaleyra.collaboration_suite_phone_ui.call.dialing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.precall.viewmodel.PreCallViewModel

internal class DialingViewModel(configure: suspend () -> Configuration): PreCallViewModel<DialingUiState>(configure) {

    override fun initialState() = DialingUiState()

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DialingViewModel(configure) as T
                }
            }
    }
}
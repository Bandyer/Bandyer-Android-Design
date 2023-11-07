package com.kaleyra.video_sdk.call.dialing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.precall.viewmodel.PreCallViewModel

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
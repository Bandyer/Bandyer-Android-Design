package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import kotlinx.coroutines.flow.flatMapLatest

internal class FileShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<FileShareUiState>(configure) {
    override fun initialState() = FileShareUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    init {

    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FileShareViewModel(configure) as T
                }
            }
    }
}




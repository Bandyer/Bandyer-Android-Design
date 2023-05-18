package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.sharedfolder.SharedFolder
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions.getFileSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick.FilePickProvider
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.FileShareMapper.toSharedFilesUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*

internal class FileShareViewModel(configure: suspend () -> Configuration, filePickProvider: FilePickProvider) : BaseViewModel<FileShareUiState>(configure) {
    override fun initialState() = FileShareUiState()

    private val sharedFolder: SharedFolder?
        get() = call.getValue()?.sharedFolder

    init {
        filePickProvider.fileUri
            .debounce(300)
            .onEach { uri ->
                if (uri.getFileSize() > MaxFileUploadBytes) _uiState.update { it.copy(showFileSizeLimit = true) }
                else {
                    upload(uri)
                    phoneBox.getValue()?.showCall()
                }
            }
            .launchIn(viewModelScope)

        call
            .toSharedFilesUi()
            .onEach { files ->
                val list = ImmutableList(files.sortedByDescending { it.time })
                _uiState.update { it.copy(sharedFiles = list) }
            }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        sharedFolder?.cancelAll()
    }

    fun upload(uri: Uri) {
        sharedFolder?.upload(uri)
    }

    fun download(id: String) {
        sharedFolder?.download(id)
    }

    fun cancel(id: String) {
        sharedFolder?.cancel(id)
    }

    fun dismissUploadLimit() {
        _uiState.update { it.copy(showFileSizeLimit = false) }
    }

    companion object {

        const val MaxFileUploadBytes = 150 * 1000 * 1000

        fun provideFactory(configure: suspend () -> Configuration, filePickProvider: FilePickProvider) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FileShareViewModel(configure, filePickProvider) as T
                }
            }
    }
}




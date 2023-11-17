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

package com.kaleyra.video_sdk.call.fileshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions.getFileSize
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickProvider
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.toSharedFilesUi
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.*

internal class FileShareViewModel(configure: suspend () -> Configuration, filePickProvider: FilePickProvider) : BaseViewModel<FileShareUiState>(configure),
    UserMessageViewModel {

    override fun initialState() = FileShareUiState()

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    private val sharedFolder: SharedFolder?
        get() = call.getValue()?.sharedFolder

    private var onFileSelected: (() -> Unit)? = null

    init {
        filePickProvider.fileUri
            .debounce(300)
            .onEach { uri ->
                if (uri.getFileSize() > MaxFileUploadBytes) _uiState.update { it.copy(showFileSizeLimit = true) }
                else {
                    onFileSelected?.invoke()
                    upload(uri)
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

    fun setOnFileSelected(block: () -> Unit) {
        onFileSelected = block
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




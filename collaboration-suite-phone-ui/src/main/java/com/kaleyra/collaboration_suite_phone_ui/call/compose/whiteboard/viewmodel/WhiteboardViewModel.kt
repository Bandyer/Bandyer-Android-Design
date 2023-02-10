package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite.phonebox.WhiteboardView
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WhiteboardMapper.getWhiteboardTextEvents
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WhiteboardMapper.isWhiteboardLoading
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import kotlinx.coroutines.flow.*

internal class WhiteboardViewModel(configure: suspend () -> Configuration, context: Context) :
    BaseViewModel<WhiteboardUiState>(configure) {
    override fun initialState() = WhiteboardUiState()

    private val call = phoneBox
        .flatMapLatest { it.call }
        .shareInEagerly(viewModelScope)

    private val whiteboard: Whiteboard?
        get() = call.getValue()?.whiteboard

    private val onTextConfirmed = MutableStateFlow<((String) -> Unit)?>(null)

    init {
        call
            .flatMapLatest { it.state }
            .onEach {
                if (it !is Call.State.Disconnected.Ended) return@onEach
                whiteboard?.unload()
            }.launchIn(viewModelScope)

        call
            .isWhiteboardLoading()
            .onEach { isLoading -> _uiState.update { it.copy(isLoading = isLoading) } }
            .launchIn(viewModelScope)

        call
            .getWhiteboardTextEvents()
            .onEach { event ->
                val (onCompletion, text) = when (event) {
                    is Whiteboard.Event.Text.Edit -> Pair(event.completion, event.oldText)
                    is Whiteboard.Event.Text.Add -> Pair(event.completion, "")
                }
                onTextConfirmed.value = onCompletion
                _uiState.update { it.copy(text = text) }
            }.launchIn(viewModelScope)

        setUpWhiteboardView(context)
    }

    override fun onCleared() {
        super.onCleared()
        whiteboard?.view?.value = null
    }

    fun onReloadClick() {
        whiteboard?.load()
    }

    fun onTextDismissed() = resetTextState()

    fun onTextConfirmed(text: String) {
        onTextConfirmed.value?.invoke(text)
        _uiState.update { it.copy(text = null) }
    }

    fun uploadMediaFile(uri: Uri) {
        whiteboard?.addMediaFile(uri)
    }

    private fun setUpWhiteboardView(context: Context) {
        val whiteboardView = WhiteboardView(context)
        whiteboard?.view?.value = whiteboardView
        whiteboard?.load()
        _uiState.update { it.copy(whiteboardView = whiteboardView) }
    }

    private fun resetTextState() {
        onTextConfirmed.value = null
        _uiState.update { it.copy(text = null) }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration, context: Context) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WhiteboardViewModel(configure, context) as T
                }
            }
    }
}


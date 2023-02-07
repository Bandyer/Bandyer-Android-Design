package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite.phonebox.WhiteboardView
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import kotlinx.coroutines.flow.*

internal class WhiteboardViewModel(configure: suspend () -> Configuration) : BaseViewModel<WhiteboardUiState>(configure) {
    override fun initialState() = WhiteboardUiState()

    val whiteboard = phoneBox.flatMapLatest { it.call }.map { it.whiteboard }.shareInEagerly(viewModelScope)

    private val onTextConfirmedLambda = MutableStateFlow<((String) -> Unit)?>(null)

    init {

        whiteboard
            .flatMapLatest { it.state }
            .map { it is Whiteboard.State.Loading }
            .onEach { isLoading ->
                _uiState.update { it.copy(isLoading = isLoading) }
            }
            .launchIn(viewModelScope)

        whiteboard
            .flatMapLatest { it.events }
            .filterIsInstance<Whiteboard.Event.Text>()
            .onEach { event ->
                val text = when (event) {
                    is Whiteboard.Event.Text.Edit -> {
                        onTextConfirmedLambda.value = event.completion
                        event.oldText
                    }
                    is Whiteboard.Event.Text.Add -> {
                        onTextConfirmedLambda.value = event.completion
                        ""
                    }
                }
                _uiState.update { it.copy(text = text) }
            }.launchIn(viewModelScope)
    }

    fun onReloadClick() {
        val whiteboard = whiteboard.getValue() ?: return
        whiteboard.load()
    }

    fun onTextDismiss() {
        onTextConfirmedLambda.value = null
        _uiState.update { it.copy(text = null) }
    }

    fun onTextConfirm(text: String) {
        onTextConfirmedLambda.value?.invoke(text)
        _uiState.update { it.copy(text = null) }
    }

    fun onWhiteboardViewCreated(view: WhiteboardView) {
        val whiteboard = whiteboard.getValue() ?: return
        whiteboard.view.value = view
        whiteboard.load()
    }

    fun onWhiteboardViewDispose() {
        val whiteboard = whiteboard.getValue() ?: return
        whiteboard.unload()
        whiteboard.view.value = null
        onTextConfirmedLambda.value = null
        _uiState.update { it.copy(text = null) }
    }

    fun uploadMediaFile(uri: Uri) {
        val whiteboard = whiteboard.getValue() ?: return
        whiteboard.addMediaFile(uri)
    }

//    fun Flow<Call>.toWhiteboard(): Flow<Whiteboard> {
//        return map { it.whiteboard }
//    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WhiteboardViewModel(configure) as T
            }
        }
    }
}


package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareWhileSubscribed(viewModelScope)

    val phoneBox = _configuration.mapSuccess { it.phoneBox }.shareWhileSubscribed(viewModelScope)

    val chatBox = _configuration.mapSuccess { it.chatBoxUI }.shareWhileSubscribed(viewModelScope)

    val usersDescription = _configuration.mapSuccess { it.usersDescription }.shareWhileSubscribed(viewModelScope)

    init {
        viewModelScope.launch {
            _configuration.emit(configure())
        }
    }

    private inline fun <T> Flow<Configuration>.mapSuccess(crossinline block: (Configuration.Success) -> T): Flow<T> =
        filterIsInstance<Configuration.Success>().map { block(it) }

    protected fun <T> Flow<T>.shareWhileSubscribed(scope: CoroutineScope): SharedFlow<T> =
        shareIn(scope, SharingStarted.Eagerly, 1)
}

sealed class Configuration {
    data class Success(val phoneBox: PhoneBoxUI, val chatBoxUI: ChatBoxUI, val usersDescription: UsersDescription) : Configuration()
    object Failure : Configuration()
}

suspend fun requestConfigure(): Configuration {
    if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
    return if (CollaborationUI.isConfigured) Configuration.Success(CollaborationUI.phoneBox, CollaborationUI.chatBox, CollaborationUI.usersDescription)
    else Configuration.Failure
}
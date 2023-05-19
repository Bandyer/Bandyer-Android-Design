package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareInEagerly(viewModelScope)

    val phoneBox = _configuration.mapSuccess { it.phoneBox }.shareInEagerly(viewModelScope)

    val chatBox = _configuration.mapSuccess { it.chatBoxUI }.shareInEagerly(viewModelScope)

    val company = _configuration.mapSuccess { it.company }.shareInEagerly(viewModelScope)

    val usersDescription = _configuration.mapSuccess { it.usersDescription }.shareInEagerly(viewModelScope)

    init {
        viewModelScope.launch {
            _configuration.emit(configure())
        }
    }

    private inline fun <T> Flow<Configuration>.mapSuccess(crossinline block: (Configuration.Success) -> T): Flow<T> =
        filterIsInstance<Configuration.Success>().map { block(it) }

    protected fun <T> Flow<T>.shareInEagerly(scope: CoroutineScope): SharedFlow<T> =
        this@shareInEagerly.shareIn(scope, SharingStarted.Eagerly, 1)

    protected fun <T> SharedFlow<T>.getValue(): T? =
        replayCache.firstOrNull()
}

sealed class Configuration {
    data class Success(val phoneBox: PhoneBoxUI, val chatBoxUI: ChatBoxUI, val company: Company, val usersDescription: UsersDescription) : Configuration()
    object Failure : Configuration()
}

suspend fun requestConfiguration(): Configuration {
    if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
    return if (CollaborationUI.isConfigured) Configuration.Success(CollaborationUI.phoneBox, CollaborationUI.chatBox, CollaborationUI.company, CollaborationUI.usersDescription)
    else Configuration.Failure
}
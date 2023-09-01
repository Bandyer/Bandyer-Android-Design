package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Company
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareInEagerly(viewModelScope)

    val conference = _configuration.mapSuccess { it.conference }.shareInEagerly(viewModelScope)

    val conversation = _configuration.mapSuccess { it.conversationUI }.shareInEagerly(viewModelScope)

    val company = _configuration.mapSuccess { it.company }.shareInEagerly(viewModelScope)

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
    data class Success(val conference: ConferenceUI, val conversationUI: ConversationUI, val company: Company) : Configuration()
    object Failure : Configuration()
}

suspend fun requestConfiguration(): Configuration {
    if (!KaleyraVideo.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
    return if (KaleyraVideo.isConfigured) Configuration.Success(KaleyraVideo.conference, KaleyraVideo.conversation, KaleyraVideo.collaboration?.company ?: NoOpCompany())
    else Configuration.Failure
}
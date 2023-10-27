package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    sealed class Configuration {
        data class Success(val conference: ConferenceUI, val conversation: ConversationUI, val company: Company, val connectedUser: StateFlow<User?>) : Configuration()
        data object Failure : Configuration()
    }

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareInEagerly(viewModelScope)

    val conference = _configuration.mapSuccess { it.conference }.shareInEagerly(viewModelScope)

    val conversation = _configuration.mapSuccess { it.conversation }.shareInEagerly(viewModelScope)

    val company = _configuration.mapSuccess { it.company }.shareInEagerly(viewModelScope)

    val connectedUser = _configuration.filterIsInstance<Configuration.Success>().flatMapLatest { it.connectedUser }.shareInEagerly(viewModelScope)

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

suspend fun requestConfiguration(): Configuration {
    if (!KaleyraVideo.isConfigured) KaleyraVideoService.get()?.onRequestKaleyraVideoConfigure()
    return if (KaleyraVideo.isConfigured) {
        Configuration.Success(
            conference = KaleyraVideo.conference,
            conversation = KaleyraVideo.conversation,
            company = KaleyraVideo.collaboration?.company ?: NoOpCompany(),
            connectedUser = KaleyraVideo.connectedUser
        )
    }
    else Configuration.Failure
}
package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startMicrophone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CallViewModel(configure: suspend () -> Configuration) :
    BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val maxFeatured = MutableStateFlow(1)

    private val streams = call
        .flatMapLatest { it.participants }
        .reduceToStreamsUi()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    init {
        // TODO add watermark

        val myStreamsIds = call
            .flatMapLatest { it.participants }
            .flatMapLatest { it.me.streams }
            .map { streams -> streams.map { it.id } }

        var featuredStreams = listOf<StreamUi>()
        var thumbnailsStreams = listOf<StreamUi>()
        streams
           .onEach { streams ->
               val added = streams - featuredStreams.toSet() - thumbnailsStreams.toSet()
               val removedFeatured = featuredStreams - streams.toSet()
               val removedThumbnails = thumbnailsStreams - streams.toSet()
               val newFeatured = (featuredStreams + added - removedFeatured.toSet()).take(maxFeatured.value)
               val newThumbnails = thumbnailsStreams + added - newFeatured.toSet() - removedThumbnails.toSet()
               featuredStreams = newFeatured
               thumbnailsStreams = newThumbnails
               _uiState.update {
                   it.copy(featuredStreams = ImmutableList(newFeatured), thumbnailStreams = ImmutableList(newThumbnails))
               }
           }
           .launchIn(viewModelScope)
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}
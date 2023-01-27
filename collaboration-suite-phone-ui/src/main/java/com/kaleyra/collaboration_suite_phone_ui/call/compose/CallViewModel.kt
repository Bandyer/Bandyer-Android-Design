package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.*

class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure) {

    override fun initialState() = CallUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val _maxFeatured = MutableStateFlow(1)

    init {
        val myStreamsIds = call
            .flatMapLatest { it.participants }
            .flatMapLatest { it.me.streams }
            .map { streams -> streams.map { it.id } }

        val streams = call
            .flatMapLatest { it.participants }
            .reduceToStreamsUi()

        combine(streams, myStreamsIds, _maxFeatured) { streams, myStreamsIds, maxFeatured ->
            val featuredStreams = 
            streams

        }.launchIn(viewModelScope)
    }

    fun setNumberOfFeaturedStreams(number: Int) {
        _maxFeatured.value = number
    }

    fun moveThumbnailToFeatured() {

    }
}
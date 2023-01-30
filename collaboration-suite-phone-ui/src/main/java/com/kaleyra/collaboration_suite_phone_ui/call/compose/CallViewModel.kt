package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.requestCameraPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.requestMicPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    fun requestMicrophonePermission(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.requestMicPermission(context)
        }
    }

    fun requestCameraPermission(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.requestCameraPermission(context)
        }
    }

    fun setNumberOfFeaturedStreams(number: Int) {
        _maxFeatured.value = number
    }

    fun moveThumbnailToFeatured() {

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
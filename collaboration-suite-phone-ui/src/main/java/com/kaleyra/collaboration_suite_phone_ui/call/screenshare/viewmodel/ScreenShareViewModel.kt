package com.kaleyra.collaboration_suite_phone_ui.call.screenshare.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite.conference.Inputs
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareUiState
import kotlinx.coroutines.launch

internal class ScreenShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<ScreenShareUiState>(configure) {
    override fun initialState() = ScreenShareUiState()

    fun shareApplicationScreen(context: Context) = shareScreen(context, Inputs.Type.Application)

    fun shareDeviceScreen(context: Context) = shareScreen(context, Inputs.Type.Screen)

    private fun shareScreen(context: Context, inputType: Inputs.Type) {
        viewModelScope.launch {
            val call = call.getValue()
            if (context !is FragmentActivity || call == null) return@launch
            val input = call.inputs
                .request(context, inputType)
                .getOrNull<Input.Video.My>() ?: return@launch
            input.tryEnable()

            val me = call.participants.value.me
            val stream = me.streams.value.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID } ?: me.addStream(
                SCREEN_SHARE_STREAM_ID
            )
            stream.video.value = input
            stream.open()
        }
    }

    companion object {

        const val SCREEN_SHARE_STREAM_ID = "screenshare"

        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScreenShareViewModel(configure) as T
                }
            }
    }
}
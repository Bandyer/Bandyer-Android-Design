package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class ScreenShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<ScreenShareUiState>(configure) {
    override fun initialState() = ScreenShareUiState()

    private val call: CallUI?
        get() = phoneBox.getValue()?.call?.getValue()

    fun shareScreen(context: Context, target: ScreenShareTargetUi) {
        if (context !is FragmentActivity) return
        viewModelScope.launch {
            when (target) {
                ScreenShareTargetUi.Application -> shareApplicationScreen(context)
                ScreenShareTargetUi.Device -> shareDeviceScreen(context)
            }
        }
    }

    private suspend fun shareApplicationScreen(context: FragmentActivity) {
        val result = call?.inputs?.request(context, Inputs.Type.Application)
        val input = result?.getOrNull<Input.Video.Application>()
        input?.tryEnable()
    }

    private suspend fun shareDeviceScreen(context: FragmentActivity) {
        val result = call?.inputs?.request(context, Inputs.Type.Screen)
        val input = result?.getOrNull<Input.Video.Screen>()
        input?.tryEnable()
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScreenShareViewModel(configure) as T
                }
            }
    }
}
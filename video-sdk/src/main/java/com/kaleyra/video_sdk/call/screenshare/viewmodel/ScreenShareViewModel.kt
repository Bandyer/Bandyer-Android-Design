/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.screenshare.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import kotlinx.coroutines.launch

internal class ScreenShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<ScreenShareUiState>(configure) {
    override fun initialState() = ScreenShareUiState()

    fun shareApplicationScreen(context: Context) = shareScreen(context, Inputs.Type.Application)

    fun shareDeviceScreen(context: Context) = shareScreen(context, Inputs.Type.Screen)

    private fun shareScreen(context: Context, inputType: Inputs.Type) {
        viewModelScope.launch {
            val call = call.getValue()
            if (context !is FragmentActivity || call == null) return@launch
            val me = call.participants.value.me ?: return@launch
            val input = call.inputs
                .request(context, inputType)
                .getOrNull<Input.Video.My>() ?: return@launch
            input.tryEnable()
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
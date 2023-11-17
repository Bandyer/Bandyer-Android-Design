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

package com.kaleyra.demo_video_sdk.ui.custom_views

import android.os.Parcelable
import com.kaleyra.video_common_ui.CallUI
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class CallConfiguration(
    val actions: Set<ConfigAction> = CallUI.Action.all.mapToConfigActions(),
    val options: CallOptions = CallOptions()
) : Parcelable {

    @Serializable
    @Parcelize
    data class CallOptions(
        val recordingEnabled: Boolean = false,
        val feedbackEnabled: Boolean = false,
        val backCameraAsDefault: Boolean = false
    ): Parcelable

    fun encode(): String = Json.encodeToString(this)

    companion object {
        fun decode(data: String): CallConfiguration = Json.decodeFromString(data)
    }
}

@Serializable
sealed class ConfigAction : Parcelable {

    companion object {

        val all by lazy {
            setOf(
                ToggleMicrophone,
                ToggleCamera,
                SwitchCamera,
                HangUp,
                FileShare,
                ScreenShare,
                Audio,
                ChangeZoom,
                ChangeVolume,
                ToggleFlashlight,
                OpenChat.ViewOnly,
                OpenChat.Full,
                ShowParticipants,
                OpenWhiteboard.ViewOnly,
                OpenWhiteboard.Full
            )
        }

        val default by lazy {
            setOf(
                ToggleMicrophone,
                ToggleCamera,
                SwitchCamera,
                HangUp,
                Audio,
                ChangeVolume,
                ShowParticipants
            )
        }
    }

    @Serializable
    @Parcelize
    data object ChangeVolume : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleCamera : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleMicrophone : ConfigAction()

    @Serializable
    @Parcelize
    data object SwitchCamera : ConfigAction()

    @Serializable
    @Parcelize
    data object HangUp : ConfigAction()

    @Serializable
    @Parcelize
    data object FileShare : ConfigAction()

    @Serializable
    @Parcelize
    data object ScreenShare : ConfigAction()

    @Serializable
    @Parcelize
    data object Audio : ConfigAction()

    @Serializable
    @Parcelize
    data object ChangeZoom : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleFlashlight : ConfigAction()

    @Serializable
    @Parcelize
    data object ShowParticipants : ConfigAction()

    @Serializable
    sealed class OpenChat : ConfigAction() {

        @Serializable
        @Parcelize
        data object ViewOnly : OpenChat()

        @Serializable
        @Parcelize
        data object Full : OpenChat()
    }

    @Serializable
    sealed class OpenWhiteboard : ConfigAction() {
        @Serializable
        @Parcelize
        data object ViewOnly : OpenWhiteboard()

        @Serializable
        @Parcelize
        data object Full : OpenWhiteboard()
    }
}

fun Set<CallUI.Action>.mapToConfigActions(): Set<ConfigAction> {
    return map { action ->
        when (action) {
            CallUI.Action.Audio -> ConfigAction.Audio
            CallUI.Action.ChangeVolume -> ConfigAction.ChangeVolume
            CallUI.Action.ChangeZoom -> ConfigAction.ChangeZoom
            CallUI.Action.FileShare -> ConfigAction.FileShare
            CallUI.Action.HangUp -> ConfigAction.HangUp
            CallUI.Action.OpenChat.Full -> ConfigAction.OpenChat.Full
            CallUI.Action.OpenWhiteboard.Full -> ConfigAction.OpenWhiteboard.Full
            CallUI.Action.OpenChat.ViewOnly -> ConfigAction.OpenChat.Full
            CallUI.Action.OpenWhiteboard.ViewOnly -> ConfigAction.OpenWhiteboard.ViewOnly
            CallUI.Action.ScreenShare -> ConfigAction.ScreenShare
            CallUI.Action.ShowParticipants -> ConfigAction.ShowParticipants
            CallUI.Action.SwitchCamera -> ConfigAction.SwitchCamera
            CallUI.Action.ToggleCamera -> ConfigAction.ToggleCamera
            CallUI.Action.ToggleFlashlight -> ConfigAction.ToggleFlashlight
            CallUI.Action.ToggleMicrophone -> ConfigAction.ToggleMicrophone
        }
    }.toSet()
}

fun Set<ConfigAction>.mapToCallUIActions(): Set<CallUI.Action> {
    return map { action ->
        when (action) {
            ConfigAction.Audio -> CallUI.Action.Audio
            ConfigAction.ChangeVolume -> CallUI.Action.ChangeVolume
            ConfigAction.ChangeZoom -> CallUI.Action.ChangeZoom
            ConfigAction.FileShare -> CallUI.Action.FileShare
            ConfigAction.HangUp -> CallUI.Action.HangUp
            ConfigAction.OpenChat.Full -> CallUI.Action.OpenChat.Full
            ConfigAction.OpenWhiteboard.Full -> CallUI.Action.OpenWhiteboard.Full
            ConfigAction.OpenChat.ViewOnly -> CallUI.Action.OpenChat.ViewOnly
            ConfigAction.OpenWhiteboard.ViewOnly -> CallUI.Action.OpenWhiteboard.ViewOnly
            ConfigAction.ScreenShare -> CallUI.Action.ScreenShare
            ConfigAction.ShowParticipants -> CallUI.Action.ShowParticipants
            ConfigAction.SwitchCamera -> CallUI.Action.SwitchCamera
            ConfigAction.ToggleCamera -> CallUI.Action.ToggleCamera
            ConfigAction.ToggleFlashlight -> CallUI.Action.ToggleFlashlight
            ConfigAction.ToggleMicrophone -> CallUI.Action.ToggleMicrophone
        }
    }.toSet()
}
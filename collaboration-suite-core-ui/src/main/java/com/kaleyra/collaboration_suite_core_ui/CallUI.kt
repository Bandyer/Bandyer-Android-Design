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

package com.kaleyra.collaboration_suite_core_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Call.PreferredType
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.mapToStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * The Call UI
 *
 * @property actions The MutableStateFlow containing the set of actions
 * @constructor
 */
class CallUI(
    private val call: Call,
    val actions: MutableStateFlow<Set<Action>> = call.getDefaultActions()
) : Call by call {

    /**
     * A property that returns true if the call is a link call.
     **/
    val isLink: Boolean get() = call is Call.Link

    /**
     * A property that indicates whether the user feedback is asked at the end of call.
     **/
    var withFeedback: Boolean = false

    /**
     * A property that indicates whether to enable or disable the proximity sensor in call behaviour.
     **/
    var disableProximitySensor: Boolean = false

    /**
     * The call action sealed class
     */
    @Keep
    sealed class Action : Parcelable {

        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
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
        }

        /**
         * Change volume action
         */
        @Parcelize
        object ChangeVolume : Action()

        /**
         * Toggle camera action
         */
        @Parcelize
        object ToggleCamera : Action()

        /**
         * Toggle microphone action
         */
        @Parcelize
        object ToggleMicrophone : Action()

        /**
         * Switch camera action
         */
        @Parcelize
        object SwitchCamera : Action()

        @Parcelize
        object HangUp : Action()

        @Parcelize
        object FileShare : Action()

        @Parcelize
        object ScreenShare : Action()

        @Parcelize
        object Audio : Action()

        /**
         * Change zoom action
         */
        @Parcelize
        object ChangeZoom : Action()

        /**
         * Toggle flashlight action
         */
        @Parcelize
        object ToggleFlashlight : Action()

        /**
         * Show participants action
         */
        @Parcelize
        object ShowParticipants : Action()

        /**
         * Open chat action
         */
        sealed class OpenChat : Action() {
            /**
             * Open chat action with view only
             */
            @Parcelize
            object ViewOnly : OpenChat()

            @Parcelize
            object Full : OpenChat()
        }

        /**
         * Open whiteboard action
         */
        sealed class OpenWhiteboard : Action() {
            /**
             * Open whiteboard action with view only
             */
            @Parcelize
            object ViewOnly : OpenWhiteboard()

            @Parcelize
            object Full : OpenChat()
        }
    }
}

private fun Call.getDefaultActions(): MutableStateFlow<Set<Action>> {
    val actions = MutableStateFlow(buildSet {
        add(CallUI.Action.ChangeVolume)
        add(CallUI.Action.ShowParticipants)
    })
    preferredType.onEach {
        actions.value = it.addActions(actions.value)
    }.launchIn(MainScope())
    return actions
}

private fun PreferredType.addActions(actions: Set<Action>) = buildSet {
    if (this@addActions.hasAudio()) add(CallUI.Action.ToggleMicrophone)
    if (this@addActions.hasVideo()) {
        add(CallUI.Action.ToggleCamera)
        add(CallUI.Action.SwitchCamera)
    }
    addAll(actions)
}


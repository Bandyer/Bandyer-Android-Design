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

import android.content.Context
import android.os.Parcelable
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

/**
 * The Call UI
 *
 * @property actions The MutableStateFlow containing the set of actions
 * @constructor
 */
class CallUI(
    private val call: Call,
    val activityClazz: Class<*>,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(Action.default)
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
     * Show the call ui
     */
    fun show() {
        if (!AppLifecycle.isInForeground.value) return
        KaleyraUIProvider.startCallActivity(activityClazz)
    }

    internal fun internalShow() {
        if (!canShowCallActivity(ContextRetainer.context, this)) return
        KaleyraUIProvider.startCallActivity(activityClazz)
    }

    private fun canShowCallActivity(context: Context, call: CallUI): Boolean {
        val participants = call.participants.value
        val creator = participants.creator()
        val isOutgoing = creator == participants.me
        return AppLifecycle.isInForeground.value && (!context.isDND() || (context.isDND() && isOutgoing)) && (!context.isSilent() || (context.isSilent() && (isOutgoing || call.isLink)))
    }

    sealed class DisplayMode {
        data object PictureInPicture: DisplayMode()

        data object Foreground: DisplayMode()

        data object Background: DisplayMode()
    }

    /**
     * The call action sealed class
     */
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


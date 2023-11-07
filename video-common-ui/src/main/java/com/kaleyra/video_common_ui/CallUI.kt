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

package com.kaleyra.video_common_ui

import android.os.Parcelable
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class DisplayModeEvent(val id: String, val displayMode: CallUI.DisplayMode)

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

    private val _displayModeEvent = MutableSharedFlow<DisplayModeEvent>(1, 1, BufferOverflow.DROP_OLDEST)

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
     * The display events successfully sent
     */
    val displayModeEvent: SharedFlow<DisplayModeEvent> = _displayModeEvent.asSharedFlow()

    /**
     * Set the activity display mode
     *
     * @param displayMode The DisplayMode
     * @return Boolean True if the activity was running and the display mode was successfully applied, false otherwise
     */
    fun setDisplayMode(displayMode: DisplayMode): Boolean {
        val context = ContextRetainer.context
        val isCallActivityRunning = context.isActivityRunning(activityClazz)
        // emit the value only if the call activity is currently running
        // to avoid the value is read from the shared flow when recreating the activity again
        // (e.g. tap the call notification when the call activity was previously destroyed)
        if (isCallActivityRunning) {
            _displayModeEvent.tryEmit(DisplayModeEvent(UUID.randomUUID().toString(), displayMode))
        }
        return isCallActivityRunning
    }

    /**
     * Show the call ui
     */
    fun show(): Boolean {
        val isInForeground = AppLifecycle.isInForeground.value
        if (isInForeground) {
            KaleyraUIProvider.startCallActivity(activityClazz)
        }
        return isInForeground
    }

    sealed class DisplayMode {
        data object PictureInPicture : DisplayMode()

        data object Foreground : DisplayMode()

        data object Background : DisplayMode()
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
        data object ChangeVolume : Action()

        /**
         * Toggle camera action
         */
        @Parcelize
        data object ToggleCamera : Action()

        /**
         * Toggle microphone action
         */
        @Parcelize
        data object ToggleMicrophone : Action()

        /**
         * Switch camera action
         */
        @Parcelize
        data object SwitchCamera : Action()

        @Parcelize
        data object HangUp : Action()

        @Parcelize
        data object FileShare : Action()

        @Parcelize
        data object ScreenShare : Action()

        @Parcelize
        data object Audio : Action()

        /**
         * Change zoom action
         */
        @Parcelize
        data object ChangeZoom : Action()

        /**
         * Toggle flashlight action
         */
        @Parcelize
        data object ToggleFlashlight : Action()

        /**
         * Show participants action
         */
        @Parcelize
        data object ShowParticipants : Action()

        /**
         * Open chat action
         */
        sealed class OpenChat : Action() {
            /**
             * Open chat action with view only
             */
            @Parcelize
            data object ViewOnly : OpenChat()

            @Parcelize
            data object Full : OpenChat()
        }

        /**
         * Open whiteboard action
         */
        sealed class OpenWhiteboard : Action() {
            /**
             * Open whiteboard action with view only
             */
            @Parcelize
            data object ViewOnly : OpenWhiteboard()

            @Parcelize
            data object Full : OpenChat()
        }
    }
}

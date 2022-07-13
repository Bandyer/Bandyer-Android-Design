package com.kaleyra.collaboration_suite_core_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite.phonebox.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

/**
 * The Call UI
 *
 * @property actions The MutableStateFlow containing the set of actions
 * @constructor
 */
class CallUI(
    private val call: Call,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(call.getDefaultActions())
) : Call by call {

    val isLink: Boolean get() = call is Call.Link

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
                    ChangeZoom,
                    ChangeVolume,
                    ToggleFlashlight,
                    OpenChat.ViewOnly,
                    ShowParticipants,
                    OpenWhiteboard.ViewOnly
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
        }
    }
}

private fun Call.getDefaultActions() = mutableSetOf<CallUI.Action>().apply {
    if (extras.preferredType.hasAudio()) add(CallUI.Action.ToggleMicrophone)
    if (extras.preferredType.hasVideo()) {
        add(CallUI.Action.ToggleCamera)
        add(CallUI.Action.SwitchCamera)
    }
    add(CallUI.Action.ChangeVolume)
    add(CallUI.Action.ShowParticipants)
}

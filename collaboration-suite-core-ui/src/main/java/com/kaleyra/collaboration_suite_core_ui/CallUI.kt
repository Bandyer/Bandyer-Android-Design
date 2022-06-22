package com.kaleyra.collaboration_suite_core_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite.phonebox.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

class CallUI(
    call: Call,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(call.getDefaultActions())
) : Call by call {

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
                    ToggleFlashlight,
                    OpenChat.ViewOnly,
                    ShowParticipants,
                    OpenWhiteboard.ViewOnly
                )
            }
        }

        @Parcelize
        object ChangeVolume : Action()

        @Parcelize
        object ToggleCamera : Action()

        @Parcelize
        object ToggleMicrophone : Action()

        @Parcelize
        object SwitchCamera : Action()

        @Parcelize
        object ChangeZoom : Action()

        @Parcelize
        object ToggleFlashlight : Action()

        @Parcelize
        object ShowParticipants : Action()

        sealed class OpenChat : Action() {
            @Parcelize
            object ViewOnly : OpenChat()
        }

        sealed class OpenWhiteboard : Action() {
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

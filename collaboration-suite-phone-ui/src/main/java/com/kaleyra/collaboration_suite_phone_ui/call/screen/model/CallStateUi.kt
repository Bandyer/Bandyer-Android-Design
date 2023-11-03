package com.kaleyra.collaboration_suite_phone_ui.call.screen.model

sealed class CallStateUi {

    data class Ringing(val isConnecting: Boolean = false) : CallStateUi()

    data object Dialing : CallStateUi()

    data object Connected : CallStateUi()

    data object Reconnecting : CallStateUi()

    data object Disconnecting : CallStateUi()

    sealed class Disconnected : CallStateUi() {

        companion object : Disconnected()

        sealed class Ended : Disconnected() {

            companion object : Ended()

            data object HungUp: Ended()

            data object Declined : Ended()

            data class Kicked(val adminName: String) : Ended()

            data object AnsweredOnAnotherDevice : Ended()

            data object LineBusy : Ended()

            data object Timeout : Ended()

            sealed class Error: Ended() {

                companion object : Error()

                data object Server : Error()

                data object Unknown : Error()
            }
        }
    }
}
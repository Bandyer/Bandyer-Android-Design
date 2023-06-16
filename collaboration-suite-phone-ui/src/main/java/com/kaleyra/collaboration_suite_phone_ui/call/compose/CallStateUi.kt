package com.kaleyra.collaboration_suite_phone_ui.call.compose

sealed class CallStateUi {

    data class Ringing(val isConnecting: Boolean = false) : CallStateUi()

    object Dialing : CallStateUi()

    object Connected : CallStateUi()

    object Reconnecting : CallStateUi()

    sealed class Disconnected : CallStateUi() {

        companion object : Disconnected()

        sealed class Ended : Disconnected() {

            companion object : Ended()

            object HungUp: Ended()

            object Declined : Ended()

            data class Kicked(val userId: String) : Ended()

            object AnsweredOnAnotherDevice : Ended()

            object LineBusy : Ended()

            object Timeout : Ended()

            sealed class Error: Ended() {

                companion object : Error()

                object Server : Error()

                object Unknown : Error()
            }
        }
    }
}
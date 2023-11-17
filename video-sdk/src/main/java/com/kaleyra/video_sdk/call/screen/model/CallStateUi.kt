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

package com.kaleyra.video_sdk.call.screen.model

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
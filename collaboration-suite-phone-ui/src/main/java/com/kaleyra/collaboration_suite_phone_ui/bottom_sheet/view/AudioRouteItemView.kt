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

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view

import com.kaleyra.collaboration_suite_phone_ui.R
import java.io.Serializable

/**
 * Abstraction of an audio route item view
 */
interface AudioRouteItemView {

    /**
     * The state of the audio route button
     */
    var state: AudioRouteState?
}

/**
 * States of an audio route item view
 *
 * @property value the drawable state
 * @constructor Create Audio route state
 */
sealed class AudioRouteState(val value: IntArray) : Serializable {

    /**
     * B l u e t o o t h
     *
     * @constructor
     *
     * @param value
     */
    sealed class BLUETOOTH(value: IntArray) : AudioRouteState(value) {

        /**
         * @suppress
         */
        companion object {
            /**
             * Returns the Bluetooth State corresponding to the name provided
             *
             * @param state name of the state to get
             * @return the state
             */
            fun valueOf(state: String): BLUETOOTH {
                val btState = BLUETOOTH::class.sealedSubclasses.first { it.simpleName == state }
                return btState.objectInstance ?: btState.java.newInstance()
            }
        }

        /**
         * The bluetooth device was unable to connect to the Android host device.
         */
        class FAILED : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_failed))

        /**
         * The bluetooth device is paired with the android environment, but currently unavailable for connection
         */
        class DISCONNECTED : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_disconnected))

        /**
         * The bluetooth device is paired with the android environment, but currently disconnected and
         *  available nearby.
         */
        class AVAILABLE : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_available))

        /**
         * The bluetooth device is in the process of connecting to the Android host device.
         */
        class CONNECTING : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_connecting))

        /**
         * The bluetooth device is connected to the Android host device.
         */
        class CONNECTED : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_connected))

        /**
         * The bluetooth device is connected to the Android host device and is in the process of activating itself as main
         * bluetooth device.
         */
        class ACTIVATING : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_activating))

        /**
         * The bluetooth device is connected to the Android host device and the active one.
         */
        class ACTIVE : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_active))

        /**
         * The bluetooth device is connected to the Android host device and the active one.
         * The bluetooth device is in the process of activating the audio connection.
         */
        class CONNECTING_AUDIO : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_connecting_audio))

        /**
         * The bluetooth device is connected to the Android host device and the active one.
         * Audio is coming through the device with SCO bluetooth connection.
         */
        class PLAYING_AUDIO : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_playing_audio))

        /**
         * The bluetooth device is in the process of deactivating due to a new device connection in progress.
         */
        class DEACTIVATING : BLUETOOTH(intArrayOf(R.attr.kaleyra_state_deactivating))

        /**
         * Checks if the status is active or in the process of connecting or playing audio
         * @return Boolean true if connected or playing audio, false otherwise
         */
        fun isConnectedOrPlaying(): Boolean = this is CONNECTED || this is ACTIVATING || this is ACTIVE || this is CONNECTING_AUDIO || this is PLAYING_AUDIO

        /**
         * Checks if the device is in the process of connecting to the host andrdid device or connecting the audio
         * @return Boolean true if connecting, false otherwise
         */
        fun isConnecting(): Boolean = this is ACTIVE || this is CONNECTING || this is ACTIVATING || this is CONNECTING_AUDIO

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            return if (other !is AudioRouteState) false
            else this::class.simpleName == other::class.simpleName
        }
    }

    /**
     * D e f a u l t
     *
     * @constructor
     */
    class DEFAULT : AudioRouteState(intArrayOf(R.attr.kaleyra_state_default))
}


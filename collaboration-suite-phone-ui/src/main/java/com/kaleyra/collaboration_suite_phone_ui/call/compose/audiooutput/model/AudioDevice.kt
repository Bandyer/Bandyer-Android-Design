package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class AudioDevice(open val id: String) {

    data class Bluetooth(override val id: String, val name: String?, val connectionState: BluetoothDeviceState, val batteryLevel: Int?) : AudioDevice(id = id)

    object LoudSpeaker : AudioDevice(id = LoudSpeaker::class.java.name)

    object EarPiece : AudioDevice(id = EarPiece::class.java.name)

    object WiredHeadset : AudioDevice(id = WiredHeadset::class.java.name)

    object Muted : AudioDevice(id = Muted::class.java.name)
}
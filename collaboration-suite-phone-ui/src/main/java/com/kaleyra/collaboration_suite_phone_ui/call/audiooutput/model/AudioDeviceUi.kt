package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class AudioDeviceUi(open val id: String) {

    data class Bluetooth(override val id: String, val name: String?, val connectionState: BluetoothDeviceState, val batteryLevel: Int?) : AudioDeviceUi(id = id)

    object LoudSpeaker : AudioDeviceUi(id = LoudSpeaker::class.java.name)

    object EarPiece : AudioDeviceUi(id = EarPiece::class.java.name)

    object WiredHeadset : AudioDeviceUi(id = WiredHeadset::class.java.name)

    object Muted : AudioDeviceUi(id = Muted::class.java.name)
}
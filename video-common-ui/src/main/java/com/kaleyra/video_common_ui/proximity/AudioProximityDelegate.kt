package com.kaleyra.video_common_ui.proximity

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance

interface AudioProximityDelegate {

    val audioCallSession: AudioCallSessionInstance

    fun trySwitchToEarpiece()

    fun tryRestoreToLoudspeaker()
}

internal class AudioProximityDelegateImpl(override val audioCallSession: AudioCallSessionInstance) : AudioProximityDelegate {

    private var wasLoudspeakerActive: Boolean = false

    override fun trySwitchToEarpiece() {
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    override fun tryRestoreToLoudspeaker() {
        val shouldEnableLoudspeaker = wasLoudspeakerActive && audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Loudspeaker())
        }
        wasLoudspeakerActive = false
    }

    private fun AudioCallSessionInstance.tryEnableDevice(audioOutput: AudioOutputDevice) {
        val device = getAvailableAudioOutputDevices.firstOrNull { it.javaClass == audioOutput.javaClass }
        if (device != null) changeAudioOutputDevice(device)
    }
}
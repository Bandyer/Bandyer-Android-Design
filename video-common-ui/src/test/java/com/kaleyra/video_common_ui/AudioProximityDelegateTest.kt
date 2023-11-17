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

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.video_common_ui.proximity.AudioProximityDelegateImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class AudioProximityDelegateTest {

    private val audioCallSessionMock = mockk<AudioCallSessionInstance>(relaxed = true)

    private val audioProximityDelegate = AudioProximityDelegateImpl(audioCallSessionMock)

    @Before
    fun setUp() {
        every { audioCallSessionMock.getAvailableAudioOutputDevices } returns listOf(
            AudioOutputDevice.Loudspeaker(),
            AudioOutputDevice.Earpiece(),
            AudioOutputDevice.WiredHeadset()
        )
    }

    @Test
    fun `change device to earpiece if the current one is loudspeaker`() {
        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.Loudspeaker()
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Earpiece()) }
    }

    @Test
    fun `do not change device to earpiece if the current one is not loudspeaker`() {
        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.WiredHeadset()
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 0) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Earpiece()) }
    }

    @Test
    fun `restore device to loudspeaker if it was previously active and the current device is earpiece`() {
        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.Loudspeaker()
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Earpiece()) }

        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.Earpiece()
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 1) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Loudspeaker()) }
    }

    @Test
    fun `do not restore device to loudspeaker if it was previously active and the current device is not earpiece`() {
        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.Loudspeaker()
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Earpiece()) }

        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.WiredHeadset()
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 0) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Loudspeaker()) }
    }

    @Test
    fun `do not restore device to loudspeaker if it was not previously active and the current device is earpiece`() {
        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.WiredHeadset()
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 0) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Earpiece()) }

        every { audioCallSessionMock.currentAudioOutputDevice } returns AudioOutputDevice.Earpiece()
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 0) { audioCallSessionMock.changeAudioOutputDevice(AudioOutputDevice.Loudspeaker()) }
    }
}
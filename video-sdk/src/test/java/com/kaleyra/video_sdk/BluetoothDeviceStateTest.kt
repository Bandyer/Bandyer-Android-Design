package com.kaleyra.video_sdk

import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.audiooutput.model.isConnectedOrPlaying
import com.kaleyra.video_sdk.call.audiooutput.model.isConnecting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothDeviceStateExtTest {

    @get:Rule
    var mainDispatcherRule = com.kaleyra.video_sdk.MainDispatcherRule()

    @Test
    fun availableBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Available.isConnecting())
    }

    @Test
    fun connectingBluetoothState_isConnecting_true() {
        assertEquals(true, BluetoothDeviceState.Connecting.isConnecting())
    }

    @Test
    fun connectingAudioBluetoothState_isConnecting_true() {
        assertEquals(true, BluetoothDeviceState.ConnectingAudio.isConnecting())
    }

    @Test
    fun playingAudioBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.PlayingAudio.isConnecting())
    }

    @Test
    fun connectedBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Connected.isConnecting())
    }

    @Test
    fun activatingBluetoothState_isConnecting_true() {
        assertEquals(true, BluetoothDeviceState.Activating.isConnecting())
    }

    @Test
    fun activeBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Active.isConnecting())
    }

    @Test
    fun deactivatingBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Deactivating.isConnecting())
    }

    @Test
    fun disconnectedBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Disconnected.isConnecting())
    }

    @Test
    fun failedBluetoothState_isConnecting_false() {
        assertEquals(false, BluetoothDeviceState.Failed.isConnecting())
    }

    @Test
    fun availableBluetoothState_isConnectedOrPlaying_false() {
        assertEquals(false, BluetoothDeviceState.Available.isConnectedOrPlaying())
    }

    @Test
    fun connectingBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.Connecting.isConnectedOrPlaying())
    }

    @Test
    fun connectingAudioBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.ConnectingAudio.isConnectedOrPlaying())
    }

    @Test
    fun playingAudioBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.PlayingAudio.isConnectedOrPlaying())
    }

    @Test
    fun connectedBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.Connected.isConnectedOrPlaying())
    }

    @Test
    fun activatingBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.Activating.isConnectedOrPlaying())
    }

    @Test
    fun activeBluetoothState_isConnectedOrPlaying_true() {
        assertEquals(true, BluetoothDeviceState.Active.isConnectedOrPlaying())
    }

    @Test
    fun deactivatingBluetoothState_isConnectedOrPlaying_false() {
        assertEquals(false, BluetoothDeviceState.Deactivating.isConnectedOrPlaying())
    }

    @Test
    fun disconnectedBluetoothState_isConnectedOrPlaying_false() {
        assertEquals(false, BluetoothDeviceState.Disconnected.isConnectedOrPlaying())
    }

    @Test
    fun failedBluetoothState_isConnectedOrPlaying_false() {
        assertEquals(false, BluetoothDeviceState.Failed.isConnectedOrPlaying())
    }
}
package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.AudioDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.AudioRoute
import com.kaleyra.collaboration_suite_phone_ui.call.compose.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRouteTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<AudioDevice>()))

    private var audioDevice: AudioDevice? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AudioRoute(items = items, onItemClick = { audioDevice = it })
        }
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(
            listOf(
                AudioDevice.LoudSpeaker("id1", false),
                AudioDevice.Muted("id2", isPlaying = true)
            )
        )
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).performClick()
        assertEquals(AudioDevice.LoudSpeaker::class.java, audioDevice!!.javaClass)
    }

    @Test
    fun loudSpeakerDevice_loudSpeakerItemDisplayed() {
        items = ImmutableList(listOf(AudioDevice.LoudSpeaker("", false)))
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).assertIsDisplayed()
    }

    @Test
    fun earpieceDevice_earpieceItemDisplayed() {
        items = ImmutableList(listOf(AudioDevice.EarPiece("", false)))
        val earpiece =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_earpiece)
        composeTestRule.onNodeWithText(earpiece).assertIsDisplayed()
    }

    @Test
    fun wiredHeadsetDevice_wiredHeadsetItemDisplayed() {
        items = ImmutableList(listOf(AudioDevice.WiredHeadset("", false)))
        val wiredHeadset =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_wired_headset)
        composeTestRule.onNodeWithText(wiredHeadset).assertIsDisplayed()
    }

    @Test
    fun mutedDevice_mutedItemDisplayed() {
        items = ImmutableList(listOf(AudioDevice.Muted("", false)))
        val muted =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_muted)
        composeTestRule.onNodeWithText(muted).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceWithNoName_genericBluetoothItemDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    null,
                    BluetoothDeviceState.ACTIVE,
                    30
                )
            )
        )
        val bluetooth =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth)
        composeTestRule.onNodeWithText(bluetooth).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceWithName_bluetoothItemDisplayedAsTheDeviceName() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.ACTIVE,
                    30
                )
            )
        )
        composeTestRule.onNodeWithText("customBluetooth").assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceBatteryLevel_bluetoothItemBatteryPercentageDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.ACTIVE,
                    30
                )
            )
        )
        val batteryLevel =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_battery_level)
        val batteryInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_battery_info,
            batteryLevel,
            30
        )
        composeTestRule.onNode(hasText(text = batteryInfo, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceDisconnected_deviceStateDisconnectedDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.DISCONNECTED,
                    30
                )
            )
        )
        val disconnected =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_disconnected)
        composeTestRule.onNode(hasText(text = disconnected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceFailed_deviceStateFailedDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.FAILED,
                    30
                )
            )
        )
        val failed =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_failed)
        composeTestRule.onNode(hasText(text = failed, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceAvailable_deviceStateAvailableDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.AVAILABLE,
                    30
                )
            )
        )
        val available =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_available)
        composeTestRule.onNode(hasText(text = available, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceDeactivating_deviceStateDeactivatingDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.DEACTIVATING,
                    30
                )
            )
        )
        val deactivating =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_deactivating)
        composeTestRule.onNode(hasText(text = deactivating, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActive_deviceStateConnectedDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.ACTIVE,
                    30
                )
            )
        )
        val connected =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceConnected_deviceStateConnectedDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.CONNECTED,
                    30
                )
            )
        )
        val connected =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActivating_deviceStateConnectedDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.ACTIVATING,
                    30
                )
            )
        )
        val connected =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActivating_connectingStateActivatingDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.ACTIVATING,
                    30
                )
            )
        )
        val activating =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
        val statusInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_connecting_status_info,
            activating
        )
        composeTestRule.onNode(hasText(text = statusInfo, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceConnecting_connectingStateActivatingDisplayed() {
        items = ImmutableList(
            listOf(
                AudioDevice.Bluetooth(
                    "",
                    false,
                    "customBluetooth",
                    BluetoothDeviceState.CONNECTING,
                    30
                )
            )
        )
        val activating =
            composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
        val statusInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_connecting_status_info,
            activating
        )
        composeTestRule.onNode(hasText(text = activating, substring = true)).assertIsDisplayed()
        composeTestRule.onNode(hasText(text = statusInfo, substring = true)).assertDoesNotExist()
    }
}
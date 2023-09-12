package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.view.AudioOutputItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioOutputItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var audioDevice by mutableStateOf<AudioDeviceUi>(AudioDeviceUi.LoudSpeaker)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AudioOutputItem(
                audioDevice = audioDevice,
                selected = false
            )
        }
    }

    @After
    fun tearDown() {
        audioDevice = AudioDeviceUi.LoudSpeaker
    }

    @Test
    fun loudSpeakerDevice_loudSpeakerTextDisplayed() {
        audioDevice = AudioDeviceUi.LoudSpeaker
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).assertIsDisplayed()
    }

    @Test
    fun earpieceDevice_earpieceTextDisplayed() {
        audioDevice = AudioDeviceUi.EarPiece
        val earpiece = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_earpiece)
        composeTestRule.onNodeWithText(earpiece).assertIsDisplayed()
    }

    @Test
    fun wiredHeadsetDevice_wiredHeadsetTextDisplayed() {
        audioDevice = AudioDeviceUi.WiredHeadset
        val wiredHeadset = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_wired_headset)
        composeTestRule.onNodeWithText(wiredHeadset).assertIsDisplayed()
    }

    @Test
    fun mutedDevice_mutedItemTextDisplayed() {
        audioDevice = AudioDeviceUi.Muted
        val muted = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_muted)
        composeTestRule.onNodeWithText(muted).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceWithNoName_genericBluetoothNameTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
                    id = "id",
                    name = null,
                    connectionState = BluetoothDeviceState.Active,
                    batteryLevel = 30
                )
        val bluetooth = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth)
        composeTestRule.onNodeWithText(bluetooth).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceWithName_bluetoothCustomNameDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Active,
            batteryLevel = 30
        )
        composeTestRule.onNodeWithText("customBluetooth").assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceBatteryLevel_bluetoothBatteryPercentageDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Active,
            batteryLevel = 30
        )
        val batteryLevel = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_battery_level)
        val batteryInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_battery_info,
            batteryLevel,
            30
        )
        composeTestRule.onNode(hasText(text = batteryInfo, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceDisconnected_deviceStateDisconnectedTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Disconnected,
            batteryLevel = 30
        )
        val disconnected = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_disconnected)
        composeTestRule.onNode(hasText(text = disconnected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceFailed_deviceStateFailedTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Failed,
            batteryLevel = 30
        )
        val failed = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_failed)
        composeTestRule.onNode(hasText(text = failed, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceAvailable_deviceStateAvailableTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Available,
            batteryLevel = 30
        )
        val available = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_available)
        composeTestRule.onNode(hasText(text = available, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceDeactivating_deviceStateDeactivatingTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Deactivating,
            batteryLevel = 30
        )
        val deactivating = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_deactivating)
        composeTestRule.onNode(hasText(text = deactivating, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActive_deviceStateConnectedTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Active,
            batteryLevel = 30
        )
        val connected = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceConnected_deviceStateConnectedTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Connected,
            batteryLevel = 30
        )
        val connected = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActivating_deviceStateConnectedTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Activating,
            batteryLevel = 30
        )
        val connected = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
        composeTestRule.onNode(hasText(text = connected, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceActivating_connectingStateActivatingTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Activating,
            batteryLevel = 30
        )
        val activating = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
        val statusInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_connecting_status_info,
            activating
        )
        composeTestRule.onNode(hasText(text = statusInfo, substring = true)).assertIsDisplayed()
    }

    @Test
    fun bluetoothDeviceConnecting_connectingStateActivatingTextDisplayed() {
        audioDevice = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "customBluetooth",
            connectionState = BluetoothDeviceState.Connecting,
            batteryLevel = 30
        )
        val activating = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
        val statusInfo = composeTestRule.activity.getString(
            R.string.kaleyra_bluetooth_connecting_status_info,
            activating
        )
        composeTestRule.onNode(hasText(text = statusInfo, substring = true)).assertIsDisplayed()
    }
}
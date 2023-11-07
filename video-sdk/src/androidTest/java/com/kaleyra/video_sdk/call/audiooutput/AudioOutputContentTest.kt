package com.kaleyra.video_sdk.call.audiooutput

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.audiooutput.view.AudioOutputContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.kaleyra.video_sdk.R

@RunWith(AndroidJUnit4::class)
class AudioOutputContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<AudioDeviceUi>()))

    private var audioDevice: AudioDeviceUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AudioOutputContent(
                items = items,
                playingDeviceId = null,
                onItemClick = { audioDevice = it }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        audioDevice = null
    }

    @Test
    fun loudSpeakerDevice_loudSpeakerItemDisplayed() {
        items = ImmutableList(listOf(AudioDeviceUi.LoudSpeaker))
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).assertIsDisplayed()
    }

    @Test
    fun earpieceDevice_earpieceItemDisplayed() {
        items = ImmutableList(listOf(AudioDeviceUi.EarPiece))
        val earpiece = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_earpiece)
        composeTestRule.onNodeWithText(earpiece).assertIsDisplayed()
    }

    @Test
    fun wirelessHeadsetDevice_wirelessHeadsetItemDisplayed() {
        items = ImmutableList(listOf(AudioDeviceUi.WiredHeadset))
        val wiredHeadset = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_wired_headset)
        composeTestRule.onNodeWithText(wiredHeadset).assertIsDisplayed()
    }

    @Test
    fun mutedDevice_mutedItemDisplayed() {
        items = ImmutableList(listOf(AudioDeviceUi.Muted))
        val muted = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_muted)
        composeTestRule.onNodeWithText(muted).assertIsDisplayed()
    }

    @Test
    fun bluetoothDevice_bluetoothItemDisplayed() {
        items = ImmutableList(listOf(AudioDeviceUi.Bluetooth(id = "", name = null, batteryLevel = null, connectionState = BluetoothDeviceState.Connecting)))
        val bluetooth = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_bluetooth)
        composeTestRule.onNodeWithText(bluetooth).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.EarPiece))
        val earpiece = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_earpiece)
        composeTestRule.onNodeWithText(earpiece).performClick()
        assertEquals(AudioDeviceUi.EarPiece, audioDevice)
    }
}
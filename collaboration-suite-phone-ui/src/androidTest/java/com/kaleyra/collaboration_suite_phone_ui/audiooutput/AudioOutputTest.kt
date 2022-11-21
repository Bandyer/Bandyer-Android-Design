package com.kaleyra.collaboration_suite_phone_ui.audiooutput

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.AudioOutputScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioOutputTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<AudioDeviceUi>()))

    private var audioDevice: AudioDeviceUi? = null

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AudioOutputScreen(
                uiState = AudioOutputUiState(audioDeviceList = items),
                onItemClick = { audioDevice = it },
                onBackPressed = { backPressed = true }
            )
        }
        audioDevice = null
    }

    @Test
    fun audioOutputTitleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(backPressed)
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.Muted))
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).performClick()
        assertEquals(AudioDeviceUi.LoudSpeaker::class.java, audioDevice!!.javaClass)
    }
}
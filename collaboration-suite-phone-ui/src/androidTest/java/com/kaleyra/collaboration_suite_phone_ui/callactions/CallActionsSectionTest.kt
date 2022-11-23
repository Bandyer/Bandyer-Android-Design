package com.kaleyra.collaboration_suite_phone_ui.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionsSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallAction>()))

    private var clickedAction: CallAction? = null

    private var toggled: Boolean? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallActionsSection(
                uiState = CallActionsUiState(actionList = items),
                onItemClick = { action, toggled ->
                    this.clickedAction = action
                    this.toggled = toggled
                }
            )
        }
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(CallAction.ScreenShare(), CallAction.Audio()))
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        assertEquals(CallAction.Audio::class.java, clickedAction!!::class.java)
    }

    @Test
    fun userClicksOnToggleableItem_onItemClickReturnsToggledTrue() {
        items = ImmutableList(listOf(CallAction.Microphone(), CallAction.Audio()))
        val muteMic = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_mic_description)
        composeTestRule.onNodeWithContentDescription(muteMic).performClick()
        assert(toggled == true)
    }
}
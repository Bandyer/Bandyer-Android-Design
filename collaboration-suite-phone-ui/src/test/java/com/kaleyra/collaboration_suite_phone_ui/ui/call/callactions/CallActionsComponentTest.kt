package com.kaleyra.collaboration_suite_phone_ui.ui.call.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(RobolectricTestRunner::class)
class CallActionsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallAction>()))

    private var isActionClicked: CallAction? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallActionsComponent(
                uiState = CallActionsUiState(actionList = items),
                onItemClick = { action ->
                    isActionClicked = action
                }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        isActionClicked = null
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(CallAction.ScreenShare(), CallAction.Audio()))
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        assertEquals(CallAction.Audio::class.java, isActionClicked!!::class.java)
    }
}
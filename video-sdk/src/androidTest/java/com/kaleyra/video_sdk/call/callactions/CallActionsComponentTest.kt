package com.kaleyra.video_sdk.call.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
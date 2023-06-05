package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialingComponentTest: PreCallComponentTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var uiState = mutableStateOf(PreCallUiState(video = streamUiMock.video))

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            DialingComponent(
                uiState = uiState.value,
                onBackPressed = { backPressed = true }
            )
        }
    }

    @After
    fun tearDown() {
        uiState.value = PreCallUiState(video = streamUiMock.video)
        backPressed = false
    }

    @Test
    fun usersClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_otherParticipantsUsernamesAreDisplayed() {
        uiState.value = PreCallUiState(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingSubtitleIsDisplayed() {
        val dialing = composeTestRule.activity.getString(R.string.kaleyra_call_status_ringing)
        composeTestRule.onNodeWithText(dialing).assertIsDisplayed()
    }

}
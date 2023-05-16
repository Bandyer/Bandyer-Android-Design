package com.kaleyra.collaboration_suite_phone_ui

import android.graphics.Rect
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialingComponentTest: PreCallComponentTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var uiState = mutableStateOf(PreCallUiState(stream = streamUiMock))

    private var backPressed = false

    private var streamViewRect: Rect? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            DialingComponent(
                uiState = uiState.value,
                onBackPressed = { backPressed = true },
                onStreamViewPositioned = { streamViewRect = it }
            )
        }
    }

    @After
    fun tearDown() {
        uiState.value = PreCallUiState(stream = streamUiMock)
        backPressed = false
        streamViewRect = null
    }

    @Test
    fun usersClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_otherParticipantsUsernamesAreDisplayed() {
        uiState.value = PreCallUiState(participants = listOf("user1", "user2"))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingSubtitleIsDisplayed() {
        val dialing = composeTestRule.activity.getString(R.string.kaleyra_call_status_dialing)
        composeTestRule.onNodeWithText(dialing).assertIsDisplayed()
    }

    @Test
    fun onStreamViewPositionedInvoked() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        uiState.value = PreCallUiState(stream = streamUiMock.copy(video = video))
        composeTestRule.waitForIdle()
        assertNotEquals(null, streamViewRect)
    }

}
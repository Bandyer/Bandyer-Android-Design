package com.kaleyra.collaboration_suite_phone_ui.ui.call.precall

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.streams.StreamViewTestTag
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.ui.findAvatar
import com.kaleyra.collaboration_suite_phone_ui.ui.findBackButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DialingComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var uiState by mutableStateOf(DialingUiState(video = streamUiMock.video))

    private var userMessage by mutableStateOf<UserMessage?>(null)

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            DialingComponent(
                uiState = uiState,
                userMessage = userMessage,
                onBackPressed = { backPressed = true }
            )
        }
    }

    @After
    fun tearDown() {
        uiState = DialingUiState(video = streamUiMock.video)
        backPressed = false
    }

    @Test
    fun callInfoWidgetIsDisplayed() {
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun videoNull_avatarDisplayed() {
        uiState = DialingUiState(video = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNull_avatarDisplayed() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState = DialingUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndDisabled_avatarIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false)
        uiState = DialingUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndEnabled_streamIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        uiState = DialingUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isLinkTrue_connectingIsDisplayed() {
        uiState = DialingUiState(isLink = true)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        composeTestRule.onNodeWithText(connecting).assertIsDisplayed()
    }

    @Test
    fun isConnectingTrue_connectingIsDisplayed() {
        uiState = DialingUiState(isConnecting = true)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        composeTestRule.onNodeWithText(connecting).assertIsDisplayed()
    }

    @Test
    fun usersClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_otherParticipantsUsernamesAreDisplayed() {
        uiState = DialingUiState(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingSubtitleIsDisplayed() {
        val dialing = composeTestRule.activity.getString(R.string.kaleyra_call_status_ringing)
        composeTestRule.onNodeWithText(dialing).assertIsDisplayed()
    }

    @Test
    fun isVideoIncomingTrueAndVideoIsNull_avatarIsNotDisplayed() {
        uiState = DialingUiState(isVideoIncoming = true, video = null)
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isVideoIncomingFalseAndVideoIsNull_avatarIsDisplayed() {
        uiState = DialingUiState(isVideoIncoming = false, video = null)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewIsNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = DialingUiState(video = VideoUi(id = "videoId", view = null, isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoNotNullAndVideoViewIsNullAndVideoIsEnabled_avatarIsNotDisplayed() {
        uiState = DialingUiState(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun videoViewIsNotNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = DialingUiState(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoIsEnabled_avatarIsNotDisplayed() {
        uiState = DialingUiState(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun userMessage_userMessageSnackbarIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

}
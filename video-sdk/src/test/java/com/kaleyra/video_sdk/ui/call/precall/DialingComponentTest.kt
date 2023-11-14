package com.kaleyra.video_sdk.ui.call.precall

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
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.dialing.DialingComponent
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.callinfowidget.CallInfoWidgetTag
import com.kaleyra.video_sdk.call.stream.view.core.StreamOverlayTestTag
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.ui.findAvatar
import com.kaleyra.video_sdk.ui.findBackButton
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

    private val initialState = DialingUiState(video = streamUiMock.video, participants = ImmutableList(listOf("user1", "user2")))

    private var uiState by mutableStateOf(initialState)

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
        uiState = initialState
        backPressed = false
    }

    @Test
    fun callInfoWidgetIsDisplayed() {
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun videoNull_avatarDisplayed() {
        uiState = uiState.copy(video = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNull_avatarDisplayed() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndDisabled_avatarIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndEnabled_streamIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun participantListIsEmpty_avatarIsNotDisplay() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState = uiState.copy(video = video, participants = ImmutableList())
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isLinkTrue_connectingIsDisplayed() {
        uiState = uiState.copy(isLink = true)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        composeTestRule.onNodeWithText(connecting).assertIsDisplayed()
    }

    @Test
    fun isConnectingTrue_connectingIsDisplayed() {
        uiState = uiState.copy(isConnecting = true)
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
        uiState = uiState.copy(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingSubtitleIsDisplayed() {
        val dialing = composeTestRule.activity.getString(R.string.kaleyra_call_status_ringing)
        composeTestRule.onNodeWithText(dialing).assertIsDisplayed()
    }

    @Test
    fun isVideoIncomingTrueAndVideoIsNull_avatarIsNotDisplayed() {
        uiState = uiState.copy(isVideoIncoming = true, video = null)
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isVideoIncomingFalseAndVideoIsNull_avatarIsDisplayed() {
        uiState = uiState.copy(isVideoIncoming = false, video = null)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewIsNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = null, isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoNotNullAndVideoViewIsNullAndVideoIsEnabled_avatarIsNotDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun videoViewIsNotNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoIsEnabled_avatarIsNotDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun streamViewNotNullAndVideoEnabled_overlayIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertIsDisplayed()
    }

    @Test
    fun userMessage_userMessageSnackbarIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

}
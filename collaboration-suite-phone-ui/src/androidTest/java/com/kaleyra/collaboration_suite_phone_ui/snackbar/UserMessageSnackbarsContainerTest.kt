package com.kaleyra.collaboration_suite_phone_ui.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.view.UserMessageSnackbarsContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserMessageSnackbarsContainerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var userMessages by mutableStateOf(UserMessages())

    @Before
    fun setUp() {
        composeTestRule.setContent {
            UserMessageSnackbarsContainer(userMessages = userMessages)
        }
    }

    @Test
    fun recordingUserMessage_recordingMessageIsDisplayed() {
        userMessages = UserMessages(recordingMessage = RecordingMessage.Started())
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun mutedUserMessage_mutedMessageIsDisplayed() {
        userMessages = UserMessages(mutedMessage = MutedMessage(null))
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 0, "")
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}
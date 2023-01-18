package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import org.junit.Test

abstract class PreCallComponentTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var uiState: MutableState<PreCallUiState>

    // TODO add this test
//    @Test
//    fun callInfoWidgetIsDisplayed() {
//        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
//        // Check content description rather than text because the title is a TextView under the hood
//        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
//        composeTestRule.onNodeWithContentDescription(connecting).assertIsDisplayed()
//        composeTestRule.findBackButton().assertIsDisplayed()
//    }

    @Test
    fun streamNull_avatarDisplayed() {
        uiState.value = PreCallUiState(stream = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamViewNull_avatarDisplayed() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState.value = PreCallUiState(stream = streamUiMock.copy(video = video))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamViewNotNullAndStreamHasVideoDisabled_avatarIsDisplayed() {
        val video = VideoUi(id = "videoId", view = View(composeTestRule.activity), isEnabled = false)
        uiState.value = PreCallUiState(stream = streamUiMock.copy(video = video))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamViewNotNullAndStreamHasVideoEnabled_streamIsDisplayed() {
        val video = VideoUi(id = "videoId", view = View(composeTestRule.activity), isEnabled = true)
        uiState.value = PreCallUiState(stream = streamUiMock.copy(video = video))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

}
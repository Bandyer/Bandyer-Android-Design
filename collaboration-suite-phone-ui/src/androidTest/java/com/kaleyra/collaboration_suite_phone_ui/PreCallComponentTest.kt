package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import org.junit.Test

abstract class PreCallComponentTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var uiState: MutableState<PreCallUiState>

    @Test
    fun callInfoWidgetIsDisplayed() {
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun videoNull_avatarDisplayed() {
        uiState.value = PreCallUiState(video = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNull_avatarDisplayed() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState.value = PreCallUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndDisabled_avatarIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false)
        uiState.value = PreCallUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndEnabled_streamIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        uiState.value = PreCallUiState(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

}
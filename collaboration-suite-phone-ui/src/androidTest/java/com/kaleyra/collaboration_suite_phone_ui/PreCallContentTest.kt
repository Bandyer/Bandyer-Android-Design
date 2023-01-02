package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import org.junit.Test

abstract class PreCallContentTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var stream: MutableState<StreamUi>

    abstract var callInfo: MutableState<CallInfoUi>

    @Test
    fun callInfoWidgetIsDisplayed() {
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
        // Check content description rather than text because the title is a TextView under the hood
        composeTestRule.onNodeWithContentDescription(callInfo.value.title).assertIsDisplayed()
        composeTestRule.findBackButton().assertIsDisplayed()
    }

    @Test
    fun streamViewNull_avatarDisplayed() {
        stream.value = streamUiMock.copy(view = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamViewNotNullAndStreamHasVideoDisabled_avatarIsDisplayed() {
        stream.value = streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = false)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamViewNotNullAndStreamHasVideoEnabled_streamIsDisplayed() {
        stream.value = streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = true)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

}
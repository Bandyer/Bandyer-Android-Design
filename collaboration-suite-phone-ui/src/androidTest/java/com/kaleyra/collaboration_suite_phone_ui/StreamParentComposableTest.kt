package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import org.junit.Test

abstract class StreamParentComposableTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var stream: MutableState<StreamUi>

    @Test
    fun viewNotNull_streamViewIsDisplayed() {
        stream.value = streamUiMock.copy(view = View(composeTestRule.activity))
        findStreamView().assertIsDisplayed()
    }

    @Test
    fun viewNull_streamViewDoesNotExists() {
        stream.value = streamUiMock.copy(view = null)
        findStreamView().assertDoesNotExist()
    }

    @Test
    fun streamVideoIsNotEnabled_avatarIsDisplayed() {
        stream.value = streamUiMock.copy(isVideoEnabled = false)
        findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamVideoIsEnabled_avatarDoesNotExists() {
        stream.value = streamUiMock.copy(isVideoEnabled = true)
        findAvatar().assertDoesNotExist()
    }

    private fun findStreamView(): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag(StreamViewTestTag)
    }

    private fun findAvatar(): SemanticsNodeInteraction {
        val avatar = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        return composeTestRule.onNodeWithContentDescription(avatar)
    }
}
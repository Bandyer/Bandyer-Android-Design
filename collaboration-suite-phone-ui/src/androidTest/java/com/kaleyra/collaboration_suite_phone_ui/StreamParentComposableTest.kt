package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import io.mockk.mockk
import org.junit.Test

abstract class StreamParentComposableTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var stream: MutableState<StreamUi>

    @Test
    fun viewNotNullAndVideoIsEnabled_streamViewIsDisplayed() {
        val video =  VideoUi(id = "videoId", view = View(composeTestRule.activity), isEnabled = true)
        stream.value = streamUiMock.copy(video = video)
        findStreamView().assertIsDisplayed()
    }

    @Test
    fun viewNull_streamViewDoesNotExists() {
        val video =  VideoUi(id = "videoId", view = null, isEnabled = false)
        stream.value = streamUiMock.copy(video = video)
        findStreamView().assertDoesNotExist()
    }

    @Test
    fun viewNotNullAndStreamVideoIsDisabled_streamDoesNotExists() {
        val video =  VideoUi(id = "videoId", view = View(composeTestRule.activity), isEnabled = false)
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
    }

    @Test
    fun streamVideoIsDisabled_avatarIsDisplayed() {
        val video =  VideoUi(id = "videoId", view = null, isEnabled = false)
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamVideoIsEnabled_avatarDoesNotExists() {
        val video =  VideoUi(id = "videoId", view = null, isEnabled = true)
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    private fun findStreamView(): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag(StreamViewTestTag)
    }
}
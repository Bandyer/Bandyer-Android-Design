package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.MovablePointerTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.Test

abstract class StreamParentComposableTest {

    abstract val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

    abstract var stream: MutableState<StreamUi>

    @Test
    fun viewNotNullAndVideoIsEnabled_streamViewIsDisplayed() {
        val video =  VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
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
        val video =  VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false)
        stream.value = streamUiMock.copy(video = video)
        findStreamView().assertDoesNotExist()
    }

    @Test
    fun streamVideoIsDisabled_avatarIsDisplayed() {
        val video =  VideoUi(id = "videoId", view = null, isEnabled = false)
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamVideoIsEnabled_avatarDoesNotExists() {
        val video =  VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun streamVideoIsNull_avatarIsDisplayed() {
        stream.value = streamUiMock.copy(video = null)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamVideoViewIsNull_avatarIsDisplayed() {
        stream.value = streamUiMock.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity))))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun streamVideoPointerIsDisplayed() {
        val video =  VideoUi(
            id = "videoId",
            view = ImmutableView(View(composeTestRule.activity)),
            isEnabled = true,
            pointers = ImmutableList(listOf(PointerUi("username", 30f, 30f)))
        )
        stream.value = streamUiMock.copy(video = video)
        composeTestRule.onNodeWithTag(MovablePointerTag, useUnmergedTree = true).assertExists()
    }

    private fun findStreamView(): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag(StreamViewTestTag)
    }
}
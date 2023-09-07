package com.kaleyra.collaboration_suite_phone_ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailStreamsTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.performDoubleClick
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThumbnailStreamsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var clickedStream: String? = null

    private var doubleClickedStream: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ThumbnailStreams(
                streams = ImmutableList(
                    listOf(
                        streamUiMock.copy(id = "1", video = null),
                        streamUiMock.copy(id = "2", video = null),
                        streamUiMock.copy(id = "3", video = null)
                    )
                ),
                onStreamClick = { clickedStream = it },
                onStreamDoubleClick = { doubleClickedStream = it }
            )
        }
    }

    @After
    fun tearDown() {
        clickedStream = null
        doubleClickedStream = null
    }

    @Test
    fun thumbnailStreamsAreDisplayed() {
        val avatar = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        composeTestRule.onAllNodesWithContentDescription(avatar).assertCountEquals(3)
        composeTestRule.onAllNodesWithTag(ThumbnailTag).assertCountEquals(3)
    }

    @Test
    fun thumbnailStreamsReverseOrder() {
        val firstChildren = composeTestRule.onNodeWithTag(ThumbnailStreamsTag).onChildren().onFirst()
        val lastChildren = composeTestRule.onNodeWithTag(ThumbnailStreamsTag).onChildren().onLast()
        assert(firstChildren.getBoundsInRoot().left > lastChildren.getBoundsInRoot().left)
    }

    // todo understand why this fails
//    @Test
//    fun testThumbnailStreamClick() {
//        composeTestRule.onNodeWithTag(ThumbnailStreamsTag).onChildren().onFirst().performClick()
//        assertEquals("1", clickedStream)
//    }

    @Test
    fun testThumbnailStreamDoubleClick() {
        composeTestRule.onNodeWithTag(ThumbnailStreamsTag).onChildren().onFirst().performDoubleClick()
        assertEquals("1", doubleClickedStream)
    }
}
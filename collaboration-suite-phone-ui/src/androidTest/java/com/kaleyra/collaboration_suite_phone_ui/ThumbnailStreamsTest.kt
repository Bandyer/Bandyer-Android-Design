package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThumbnailStreamsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var clickedStream: StreamUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ThumbnailStreams(
                streams = ImmutableList(
                    listOf(
                        streamUiMock.copy(id = "1", isVideoEnabled = false),
                        streamUiMock.copy(id = "2", isVideoEnabled = false),
                        streamUiMock.copy(id = "3", isVideoEnabled = false)
                    )
                ),
                onStreamClick = { clickedStream = it }
            )
        }
        clickedStream = null
    }


    @Test
    fun thumbnailStreamsAreDisplayed() {
        val avatar = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        composeTestRule.onAllNodesWithContentDescription(avatar).assertCountEquals(3)
        composeTestRule.onAllNodesWithTag(ThumbnailTag).assertCountEquals(3)
    }
}
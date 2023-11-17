/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.ui.call.streams

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
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailStreams
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailStreamsTag
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailTag
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.ui.performDoubleClick
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
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
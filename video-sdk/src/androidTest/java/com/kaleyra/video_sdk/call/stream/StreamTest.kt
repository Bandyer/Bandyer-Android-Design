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

package com.kaleyra.video_sdk.call.stream

import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamOverlayTestTag
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import com.kaleyra.video_sdk.findAvatar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isAvatarVisible by mutableStateOf(false)

    private var showOverlay by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Stream(
                streamView = ImmutableView(View(composeTestRule.activity)),
                avatar = ImmutableUri(Uri.EMPTY),
                avatarVisible = isAvatarVisible,
                showOverlay = showOverlay
            )
        }
    }

    @After
    fun tearDown() {
        isAvatarVisible = false
        showOverlay = false
    }

    @Test
    fun streamViewIsDisplayed() {
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun avatarVisibleFalse_avatarIsNotDisplayed() {
        isAvatarVisible = false
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun avatarVisibleTrue_avatarIsDisplayed() {
        isAvatarVisible = true
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun avatarNotDisplayedAndShowOverlayTrue_overlayIsDisplayed() {
        isAvatarVisible = false
        showOverlay = true
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertIsDisplayed()
    }

    @Test
    fun avatarNotDisplayedAndShowOverlayFalse_overlayIsDisplayed() {
        isAvatarVisible = false
        showOverlay = false
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertDoesNotExist()
    }

    @Test
    fun avatarDisplayedAndShowOverlayTrue_overlayIsDisplayed() {
        isAvatarVisible = true
        showOverlay = true
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertDoesNotExist()
    }

    @Test
    fun avatarDisplayedAndShowOverlayFalse_overlayIsNotDisplayed() {
        isAvatarVisible = true
        showOverlay = false
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertDoesNotExist()
    }

}
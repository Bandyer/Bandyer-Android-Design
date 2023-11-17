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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailStream
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailTag
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.performDoubleClick
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThumbnailStreamTest: StreamParentComposableTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var stream = mutableStateOf(streamUiMock)

    private var isClicked = false

    private var isDoubleClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ThumbnailStream(
                stream = stream.value,
                onClick = { isClicked = true },
                onDoubleClick = { isDoubleClicked = true },
                isTesting = true
            )
        }
    }

    @After
    fun tearDown() {
        stream.value = streamUiMock
        isClicked = false
        isDoubleClicked = false
    }

    // todo understand why this fails
//    @Test
//    fun userClicksThumbnailStream_onClickIsInvoked() {
//        composeTestRule.onNodeWithTag(ThumbnailTag).performClick()
//        assert(isClicked)
//    }

    @Test
    fun userDoubleClicksThumbnailStream_onDoubleClickIsInvoked() {
        composeTestRule.onNodeWithTag(ThumbnailTag).performDoubleClick()
        assert(isDoubleClicked)
    }
}
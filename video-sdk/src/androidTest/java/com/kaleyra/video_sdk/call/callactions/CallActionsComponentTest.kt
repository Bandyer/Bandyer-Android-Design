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

package com.kaleyra.video_sdk.call.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallAction>()))

    private var isActionClicked: CallAction? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallActionsComponent(
                uiState = CallActionsUiState(actionList = items),
                onItemClick = { action ->
                    isActionClicked = action
                }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        isActionClicked = null
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(CallAction.ScreenShare(), CallAction.Audio()))
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        assertEquals(CallAction.Audio::class.java, isActionClicked!!::class.java)
    }
}
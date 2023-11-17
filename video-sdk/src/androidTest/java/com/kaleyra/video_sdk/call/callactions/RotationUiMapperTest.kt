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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.callactions.utility.mapToRotationState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotationUiMapperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun orientation0_mapToRotationState_rotation0() {
        var rotation by mutableStateOf(-1f)
        composeTestRule.setContent {
            rotation = mapToRotationState(mutableStateOf(0))
        }
        assertEquals(0f, rotation)
    }

    @Test
    fun orientation90_mapToRotationState_rotationMinus90() {
        var rotation by mutableStateOf(-1f)
        composeTestRule.setContent {
            rotation = mapToRotationState(mutableStateOf(90))
        }
        assertEquals(-90f, rotation)
    }

    @Test
    fun orientation180_mapToRotationState_rotation0() {
        var rotation by mutableStateOf(-1f)
        composeTestRule.setContent {
            rotation = mapToRotationState(mutableStateOf(180))
        }
        assertEquals(0f, rotation)
    }

    @Test
    fun orientation270_mapToRotationState_rotation90() {
        var rotation by mutableStateOf(-1f)
        composeTestRule.setContent {
            rotation = mapToRotationState(mutableStateOf(270))
        }
        assertEquals(90f, rotation)
    }
}
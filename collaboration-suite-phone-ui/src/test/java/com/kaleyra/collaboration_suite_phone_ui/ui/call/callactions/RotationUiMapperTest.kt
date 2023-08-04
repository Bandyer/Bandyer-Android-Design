package com.kaleyra.collaboration_suite_phone_ui.ui.call.callactions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.utility.mapToRotationState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(RobolectricTestRunner::class)
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
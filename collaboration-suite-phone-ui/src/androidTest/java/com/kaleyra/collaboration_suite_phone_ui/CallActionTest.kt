package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.view.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.view.mapToRotationState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var toggled by mutableStateOf(false)

    private var enabled by mutableStateOf(true)

    @Test
    fun buttonIsToggleable() {
        composeTestRule.setUpCallActionTest()
        composeTestRule.onRoot().onChildAt(0).assertIsToggleable()
    }

    @Test
    fun userPerformsClick_onToggledInvoked() {
        composeTestRule.setUpCallActionTest()
        composeTestRule.onRoot().performClick()
        assert(toggled)
    }

    @Test
    fun enabledFalse_actionDisabled() {
        composeTestRule.setUpCallActionTest()
        composeTestRule.onRoot().onChildAt(0).assertIsEnabled()
        enabled = false
        composeTestRule.onRoot().onChildAt(0).assertIsNotEnabled()
    }

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

    private fun ComposeContentTestRule.setUpCallActionTest() {
        setContent {
            CallAction(
                toggled = toggled,
                onToggle = { toggled = it },
                text = "",
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_off),
                iconDescription = "",
                enabled = enabled
            )
        }
    }
}
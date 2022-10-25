package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var toggled by mutableStateOf(false)

    private var enabled by mutableStateOf(true)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallAction(
                toggled = toggled,
                onToggled = { toggled = it },
                text = "",
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_off),
                enabled = enabled
            )
        }
    }

    @Test
    fun userPerformsClick_onToggledInvoked() {
        composeTestRule.onRoot().performClick()
        assert(toggled)
    }

    @Test
    fun enabledFalse_actionDisabled() {
        composeTestRule.onRoot().onChildAt(0).assertIsEnabled()
        enabled = false
        composeTestRule.onRoot().onChildAt(0).assertIsNotEnabled()
    }
}
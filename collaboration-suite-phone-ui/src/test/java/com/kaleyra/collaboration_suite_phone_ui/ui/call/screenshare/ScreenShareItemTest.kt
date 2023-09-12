package com.kaleyra.collaboration_suite_phone_ui.ui.call.screenshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.component.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.screenshare.view.ScreenShareItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenShareItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var screenShareTarget by mutableStateOf(ScreenShareTargetUi.Device)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ScreenShareItem(screenShareTarget = screenShareTarget)
        }
    }

    @After
    fun tearDown() {
        screenShareTarget = ScreenShareTargetUi.Device
    }

    @Test
    fun deviceScreenShare_deviceScreenShareTextDisplayed() {
        screenShareTarget = ScreenShareTargetUi.Device
        val fullDevice = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(fullDevice).assertIsDisplayed()
    }

    @Test
    fun appScreenShareTarget_appScreenShareTextDisplayed() {
        screenShareTarget = ScreenShareTargetUi.Application
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).assertIsDisplayed()
    }
}
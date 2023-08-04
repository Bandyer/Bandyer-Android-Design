package com.kaleyra.collaboration_suite_phone_ui.ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbConnectedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbDisconnectedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbNotSupportedSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(RobolectricTestRunner::class)
class UsbCameraSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testGenericUsbConnectedSnackbar() {
        composeTestRule.setContent { UsbConnectedSnackbar("") }
        val title = composeTestRule.activity.getString(R.string.kaleyra_generic_external_camera_connected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbConnectedSnackbar() {
        composeTestRule.setContent { UsbConnectedSnackbar("name") }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_connected, "name")
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbDisconnectedSnackbar() {
        composeTestRule.setContent { UsbDisconnectedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_disconnected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbNotSupportedSnackbar() {
        composeTestRule.setContent { UsbNotSupportedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_unsupported)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
}
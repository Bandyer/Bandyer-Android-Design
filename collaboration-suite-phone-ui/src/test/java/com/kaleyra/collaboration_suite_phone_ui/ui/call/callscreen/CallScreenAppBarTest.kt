package com.kaleyra.collaboration_suite_phone_ui.ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.screen.view.CallScreenAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.ui.ComposeViewModelsMockTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CallScreenAppBarTest : ComposeViewModelsMockTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var sheetComponent by mutableStateOf(BottomSheetComponent.CallActions)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenAppBar(
                currentSheetComponent = sheetComponent,
                visible = true,
                onBackPressed = { isBackPressed = true }
            )
        }
        isBackPressed = false
    }

    @Test
    fun whiteboardComponent_whiteboardAppBarDisplayed() {
        sheetComponent = BottomSheetComponent.Whiteboard
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareComponent_fileShareAppBarDisplayed() {
        sheetComponent = BottomSheetComponent.FileShare
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun whiteboardComponent_userClicksClose_onBackPressedInvoked() {
        userClicksClose_onBackPressedInvoked(BottomSheetComponent.Whiteboard)
    }

    @Test
    fun fileShareComponent_userClicksClose_onBackPressedInvoked() {
        userClicksClose_onBackPressedInvoked(BottomSheetComponent.FileShare)
    }

    private fun userClicksClose_onBackPressedInvoked(component: BottomSheetComponent) {
        sheetComponent = component
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}
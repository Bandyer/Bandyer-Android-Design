package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallScreenAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBarTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardAppBarTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CallScreenAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var bottomSheetContentState by mutableStateOf(
        BottomSheetContentState(
            initialComponent = BottomSheetComponent.CallActions,
            initialLineState = LineState.Expanded
        )
    )

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenAppBar(
                bottomSheetContentState = bottomSheetContentState,
                visible = true,
                onBackPressed = { isBackPressed = true }
            )
        }
        isBackPressed = false
    }

    @Test
    fun whiteboardComponent_whiteboardAppBarDisplayed() {
        bottomSheetContentState = BottomSheetContentState(initialComponent = BottomSheetComponent.Whiteboard, initialLineState = LineState.Expanded)
        composeTestRule.onNodeWithTag(WhiteboardAppBarTag).assertIsDisplayed()
    }

    @Test
    fun fileShareComponent_fileShareAppBarDisplayed() {
        bottomSheetContentState = BottomSheetContentState(initialComponent = BottomSheetComponent.FileShare, initialLineState = LineState.Expanded)
        composeTestRule.onNodeWithTag(FileShareAppBarTag).assertIsDisplayed()
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
        bottomSheetContentState = BottomSheetContentState(initialComponent = component, initialLineState = LineState.Expanded)
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}
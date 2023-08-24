package com.kaleyra.collaboration_suite_phone_ui.ui.call.callscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.WhiteboardVisibilityObserver
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.rememberCallScreenState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class WhiteboardVisibilityObserverTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded))

    private var isWhiteboardVisible: Boolean? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardVisibilityObserver(
                callScreenState = rememberCallScreenState(sheetContentState = sheetContentState),
                onWhiteboardVisibility = { isWhiteboardVisible = it }
            )
        }
    }

    @Test
    fun currentComponentWhiteboard_isWhiteboardVisibleTrue() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        composeTestRule.waitForIdle()
        assertEquals(true, isWhiteboardVisible)
    }

    @Test
    fun currentComponentOther_isWhiteboardVisibleFalse() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.waitForIdle()
        assertEquals(false, isWhiteboardVisible)
    }

}
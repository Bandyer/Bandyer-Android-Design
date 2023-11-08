package com.kaleyra.video_sdk.call.callscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.screen.WhiteboardVisibilityObserver
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetContentState
import com.kaleyra.video_sdk.call.bottomsheet.LineState
import com.kaleyra.video_sdk.call.screen.rememberCallScreenState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
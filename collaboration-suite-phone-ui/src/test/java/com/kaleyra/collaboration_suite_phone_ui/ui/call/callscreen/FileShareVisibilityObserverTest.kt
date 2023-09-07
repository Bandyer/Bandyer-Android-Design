package com.kaleyra.collaboration_suite_phone_ui.ui.call.callscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.collaboration_suite_phone_ui.call.FileShareVisibilityObserver
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.rememberCallScreenState
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileShareVisibilityObserverTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded))

    private var isFileShareVisible: Boolean? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareVisibilityObserver(
                callScreenState = rememberCallScreenState(sheetContentState = sheetContentState),
                onFileShareVisibility = { isFileShareVisible = it }
            )
        }
    }

    @Test
    fun currentComponentFileShare_isFileShareVisibleTrue() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        composeTestRule.waitForIdle()
        Assert.assertEquals(true, isFileShareVisible)
    }

    @Test
    fun currentComponentOther_isFileShareVisibleFalse() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.waitForIdle()
        Assert.assertEquals(false, isFileShareVisible)
    }

}
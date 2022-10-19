package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sign

@RunWith(AndroidJUnit4::class)
class BottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val peekHeight = 100.dp
    private val halfExpandedHeight = 200.dp
    private val contentHeight = 400.dp

    // test flag isCollapsed, ecc per lo stato
    // tests anchor view che è nella posizione giusta nei diversi stati del bottom sheet
    // tests padding values giusti per il contenuto dello schermo
    // tests per il content color sia per sheet che per il contenuto della pagina

    @Test
    fun initialStateCollapsed_sheetCollapsed() {
        checkInitialState(initialState = BottomSheetValue.Collapsed)
    }

    @Test
    fun initialStateHalfExpanded_sheetHalfExpanded() {
        checkInitialState(initialState = BottomSheetValue.HalfExpanded)
    }

    @Test
    fun initialStateExpanded_sheetExpanded() {
        checkInitialState(initialState = BottomSheetValue.Expanded)
    }

    private fun checkInitialState(initialState: BottomSheetValue) {
        val sheetState = BottomSheetScaffoldState(initialValue = initialState)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, initialState)
        }
    }

    @Test
    fun sheetCollapsed_performSmallSwipeUp_sheetHalfExpanded() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = 200
        )
    }

    @Test
    fun sheetCollapsed_performBigSwipeUp_sheetExpanded() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 600
        )
    }

    @Test
    fun sheetHalfExpanded_performBigSwipeUp_sheetExpanded() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 600
        )
    }

    @Test
    fun sheetHalfExpanded_performSmallSwipeDown_sheetCollapsed() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -200
        )
    }

    @Test
    fun sheetExpanded_performSmallSwipeDown_sheetHalfExpanded() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = -200
        )
    }

    @Test
    fun sheetExpanded_performBigSwipeDown_sheetCollapsed() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -600
        )
    }

    @Test
    fun sheetGesturesDisabled_performSwipe_sheetStateDoNotChange() {
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = 600,
            sheetGesturesEnabled = false
        )
    }

    private fun checkStateAfterSwipe(
        initialState: BottomSheetValue,
        targetState: BottomSheetValue,
        swipeAmount: Int,
        sheetGesturesEnabled: Boolean = true
    ) {
        val sheetState = BottomSheetScaffoldState(initialValue = initialState)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled
        )
        composeTestRule.onNode(hasTestTag(BottomSheetScaffoldTag)).performSwipe(swipeAmount)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, targetState)
        }
    }

    @Test
    fun sheetCollapsed_sheetHeightIsPeekHeight() {
        checkSheetHeight(BottomSheetValue.Collapsed, peekHeight)
    }

    @Test
    fun sheetHalfExpanded_sheetHeightIsHalfExpandedHeight() {
        checkSheetHeight(BottomSheetValue.HalfExpanded, halfExpandedHeight)
    }

    @Test
    fun sheetExpanded_sheetHeightIsContentHeight() {
        checkSheetHeight(BottomSheetValue.Expanded, contentHeight)
    }

    private fun checkSheetHeight(sheetState: BottomSheetValue, expectedHeight: Dp) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetScaffoldState(initialValue = sheetState))
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentBottom = bottomSheet.onParent().getBoundsInRoot().bottom
        val sheetBottom = composeTestRule.onNode(hasTestTag(BottomSheetTag)).getBoundsInRoot().top
        val height = parentBottom - sheetBottom
        height.assertIsEqualTo(expectedHeight, "sheet height")
    }

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        val content = @Composable {
            val sheetState = rememberBottomSheetScaffoldState(initialValue = BottomSheetValue.Expanded)
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentHeight)
                            .background(Color.Green)
                    )
                },
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Cyan)
                )
            }
        }
        restorationTester.setContent { content.invoke() }

        composeTestRule.onNode(hasTestTag(BottomSheetScaffoldTag)).performSwipe(-600)
        composeTestRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentBottom = bottomSheet.onParent().getBoundsInRoot().bottom
        val sheetBottom = composeTestRule.onNode(hasTestTag(BottomSheetTag)).getBoundsInRoot().top
        val height = parentBottom - sheetBottom
        height.assertIsEqualTo(peekHeight, "sheet height")
    }





    private fun ComposeContentTestRule.setBottomSheetScaffold(
        sheetState: BottomSheetScaffoldState,
        sheetGesturesEnabled: Boolean = true
    ) {
        setContent {
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentHeight)
                            .background(Color.Green)
                    )
                },
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
                sheetGesturesEnabled = sheetGesturesEnabled
            ) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .background(Color.Cyan)
                )
            }
        }
    }

    private fun SemanticsNodeInteraction.performSwipe(amount: Int) {
        performTouchInput {
            val startHeight = if (amount.sign == 1) height else height + amount
            val endHeight = if (amount.sign == 1) height - amount else height
            swipe(
                start = Offset(center.x, startHeight.toFloat()),
                end = Offset(center.x, endHeight.toFloat()),
                durationMillis = 200
            )
        }
    }

}
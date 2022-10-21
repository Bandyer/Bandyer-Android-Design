package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
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

    @Test
    fun initialStateCollapsed_sheetCollapsed() = checkInitialState(initialState = BottomSheetValue.Collapsed)

    @Test
    fun initialStateHalfExpanded_sheetHalfExpanded() = checkInitialState(initialState = BottomSheetValue.HalfExpanded)

    @Test
    fun initialStateExpanded_sheetExpanded() = checkInitialState(initialState = BottomSheetValue.Expanded)

    private fun checkInitialState(initialState: BottomSheetValue) {
        val sheetState = BottomSheetScaffoldState(initialValue = initialState)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, initialState)
        }
    }

    @Test
    fun sheetCollapsed_performSmallSwipeUp_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = 0.1f
        )

    @Test
    fun sheetCollapsed_performBigSwipeUp_sheetExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 0.5f
        )

    @Test
    fun sheetHalfExpanded_performBigSwipeUp_sheetExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 0.5f
        )

    @Test
    fun sheetHalfExpanded_performSmallSwipeDown_sheetCollapsed() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -0.1f
        )

    @Test
    fun sheetExpanded_performSmallSwipeDown_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = -0.1f
        )

    @Test
    fun sheetExpanded_performBigSwipeDown_sheetCollapsed() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -0.5f
        )

    @Test
    fun sheetGesturesDisabled_performSwipe_sheetStateDoNotChange() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = 0.5f,
            sheetGesturesEnabled = false
        )

    private fun checkStateAfterSwipe(
        initialState: BottomSheetValue,
        targetState: BottomSheetValue,
        swipeAmount: Float,
        sheetGesturesEnabled: Boolean = true
    ) {
        val sheetState = BottomSheetScaffoldState(initialValue = initialState)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled
        )
        composeTestRule.onRoot().performSwipe(swipeAmount)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, targetState)
        }
    }

    @Test
    fun sheetCollapsed_sheetHeightIsPeekHeight() = checkSheetHeight(BottomSheetValue.Collapsed, peekHeight)

    @Test
    fun sheetHalfExpanded_sheetHeightIsHalfExpandedHeight() = checkSheetHeight(BottomSheetValue.HalfExpanded, halfExpandedHeight)

    @Test
    fun sheetExpanded_sheetHeightIsContentHeight() = checkSheetHeight(BottomSheetValue.Expanded, contentHeight)

    private fun checkSheetHeight(sheetState: BottomSheetValue, expectedHeight: Dp) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetScaffoldState(initialValue = sheetState))
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val sheetTop = bottomSheet.getBoundsInRoot().top
        val height = parentHeight - sheetTop
        height.assertIsEqualTo(expectedHeight, "sheet height")
    }

    @Test
    fun sheetCollapsed_anchorPositionIsRight() = checkAnchorPosition(sheetValue = BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_anchorPositionIsRight() = checkAnchorPosition(sheetValue = BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_anchorPositionIsRight() = checkAnchorPosition(sheetValue = BottomSheetValue.Expanded)

    private fun checkAnchorPosition(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetScaffoldState(initialValue = sheetValue))
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val anchor = composeTestRule.onNode(hasTestTag(AnchorTag))
        val bottomSheetTop = bottomSheet.getBoundsInRoot().top
        val anchorBottom = anchor.getBoundsInRoot().bottom
        val bottomSheetRight = bottomSheet.getBoundsInRoot().right
        val anchorRight = anchor.getBoundsInRoot().right
        anchorBottom.assertIsEqualTo(bottomSheetTop, "anchor bottom position")
        anchorRight.assertIsEqualTo(bottomSheetRight, "anchor right position")
    }

    // test flag isCollapsed, ecc per lo stato
    // tests per il content color sia per sheet che per il contenuto della pagina

    @Test
    fun sheetCollapsed_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Expanded)

    private fun checkBottomSheetInsets(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetScaffoldState(initialValue = sheetValue))
        val parentHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val bottomSheetTop = composeTestRule.onNode(hasTestTag(BottomSheetTag)).getBoundsInRoot().top
        val expected = parentHeight -  bottomSheetTop
        composeTestRule.onNode(hasText("$expected"))
    }

    @Test
    fun sheetStateCollapsed_isCollapsed_true() {
        val sheetState = BottomSheetScaffoldState(initialValue = BottomSheetValue.Collapsed)
        assert(sheetState.isCollapsed)
    }

    @Test
    fun sheetStateHalfExpanded_isHalfExpanded_true() {
        val sheetState = BottomSheetScaffoldState(initialValue = BottomSheetValue.HalfExpanded)
        assert(sheetState.isHalfExpanded)
    }

    @Test
    fun sheetStateExpanded_isExpanded_true() {
        val sheetState = BottomSheetScaffoldState(initialValue = BottomSheetValue.Expanded)
        assert(sheetState.isExpanded)
    }


    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        val content = @Composable {
            val sheetState =
                rememberBottomSheetScaffoldState(initialValue = BottomSheetValue.Expanded)
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentHeight)
                    )
                },
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
        restorationTester.setContent { content.invoke() }

        composeTestRule.onRoot().performSwipe(-0.5f)
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
                    )
                },
                anchor = {
                    Box(modifier = Modifier.size(56.dp))
                },
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
                sheetGesturesEnabled = sheetGesturesEnabled
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Text("${it.asPaddingValues().calculateBottomPadding()}")
            }
        }
    }

    private fun SemanticsNodeInteraction.performSwipe(amount: Float) {
        performTouchInput {
            val startHeight = if (amount.sign == 1f) height else height + height * amount
            val endHeight = if (amount.sign == 1f) height - height * amount else height
            swipe(
                start = Offset(center.x, startHeight.toFloat()),
                end = Offset(center.x, endHeight.toFloat()),
                durationMillis = 200
            )
        }
    }

}
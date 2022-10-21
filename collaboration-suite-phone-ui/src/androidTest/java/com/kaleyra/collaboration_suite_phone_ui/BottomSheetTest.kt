package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
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
    fun initialStateCollapsed_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, BottomSheetValue.Collapsed)
            assert(sheetState.isCollapsed)
        }
    }

    @Test
    fun initialStateHalfExpanded_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, BottomSheetValue.HalfExpanded)
            assert(sheetState.isHalfExpanded)
        }
    }

    @Test
    fun initialStateExpanded_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(currentValue, BottomSheetValue.Expanded)
            assert(sheetState.isExpanded)
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
        val sheetState = BottomSheetState(initialValue = initialState)
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
    fun sheetCollapsed_sheetHeightIsPeekHeight() =
        checkSheetHeight(BottomSheetValue.Collapsed, peekHeight)

    @Test
    fun sheetHalfExpanded_sheetHeightIsHalfExpandedHeight() =
        checkSheetHeight(BottomSheetValue.HalfExpanded, halfExpandedHeight)

    @Test
    fun sheetExpanded_sheetHeightIsContentHeight() =
        checkSheetHeight(BottomSheetValue.Expanded, contentHeight)

    private fun checkSheetHeight(sheetState: BottomSheetValue, expectedHeight: Dp) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = sheetState))
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val sheetTop = bottomSheet.getBoundsInRoot().top
        val height = parentHeight - sheetTop
        height.assertIsEqualTo(expectedHeight, "sheet height")
    }

    @Test
    fun sheetCollapsed_anchorPositionIsRight() =
        checkAnchorPosition(sheetValue = BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_anchorPositionIsRight() =
        checkAnchorPosition(sheetValue = BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_anchorPositionIsRight() =
        checkAnchorPosition(sheetValue = BottomSheetValue.Expanded)

    private fun checkAnchorPosition(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = sheetValue))
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val anchor = composeTestRule.onNode(hasTestTag(AnchorTag))
        val bottomSheetTop = bottomSheet.getBoundsInRoot().top
        val anchorBottom = anchor.getBoundsInRoot().bottom
        val bottomSheetRight = bottomSheet.getBoundsInRoot().right
        val anchorRight = anchor.getBoundsInRoot().right
        anchorBottom.assertIsEqualTo(bottomSheetTop, "anchor bottom position")
        anchorRight.assertIsEqualTo(bottomSheetRight, "anchor right position")
    }

    @Test
    fun sheetCollapsed_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Expanded)

    private fun checkBottomSheetInsets(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = sheetValue))
        val parentHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val bottomSheetTop = composeTestRule.onNode(hasTestTag(BottomSheetTag)).getBoundsInRoot().top
        val expected = parentHeight - bottomSheetTop
        composeTestRule.onRoot().onChildren().onFirst().assertHeightIsEqualTo(expected)
    }

    @Test
    fun sheetCollapsed_halfExpand_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::halfExpand
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.HalfExpanded)
    }

    @Test
    fun sheetExpanded_halfExpand_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::halfExpand
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.HalfExpanded)
    }

    @Test
    fun sheetCollapsed_expand_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::expand
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.Expanded)
    }

    @Test
    fun sheetHalfExpanded_expand_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::expand
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.Expanded)
    }

    @Test
    fun sheetExpanded_collapse_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.Collapsed)
    }

    @Test
    fun sheetHalfExpanded_collapse_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(sheetState.currentValue, BottomSheetValue.Collapsed)
    }

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
             val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetContent = { Box(
                    Modifier
                        .fillMaxWidth()
                        .height(contentHeight) )},
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
                content = { }
            )
        }

        composeTestRule.onRoot().performSwipe(0.5f)
        composeTestRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNode(hasTestTag(BottomSheetTag)).assertHeightIsEqualTo(contentHeight)
    }

    private fun ComposeContentTestRule.setBottomSheetScaffold(
        sheetState: BottomSheetState,
        sheetGesturesEnabled: Boolean = true,
        launchedEffect: suspend () -> Unit = { }
    ) {
        setContent {
            LaunchedEffect(Unit) {
                launchedEffect.invoke()
            }
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
                        .windowInsetsPadding(it)
                )
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
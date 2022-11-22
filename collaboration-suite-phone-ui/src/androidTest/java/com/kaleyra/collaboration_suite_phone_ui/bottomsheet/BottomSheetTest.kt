package com.kaleyra.collaboration_suite_phone_ui.bottomsheet

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.min
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
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
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val peekHeight = 100.dp
    private val halfExpandedHeight = 200.dp
    private val contentHeight = 400.dp

    private var sheetInsets by mutableStateOf(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp))

    @Test
    fun initialStateCollapsed_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Collapsed, currentValue)
            assert(sheetState.isCollapsed)
        }
    }

    @Test
    fun initialStateHalfExpanded_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, currentValue)
            assert(sheetState.isHalfExpanded)
        }
    }

    @Test
    fun initialStateExpanded_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Expanded, currentValue)
            assert(sheetState.isExpanded)
        }
    }

    @Test
    fun sheetCollapsed_userPerformsSmallSwipeUp_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = 0.1f
        )

    @Test
    fun sheetCollapsed_userPerformsBigSwipeUp_sheetExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 0.5f
        )

    @Test
    fun sheetHalfExpanded_userPerformsBigSwipeUp_sheetExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Expanded,
            swipeAmount = 0.5f
        )

    @Test
    fun sheetHalfExpanded_userPerformsSmallSwipeDown_sheetCollapsed() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -0.1f
        )

    @Test
    fun sheetExpanded_userPerformsSmallSwipeDown_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = -0.1f
        )

    @Test
    fun sheetExpanded_userPerformsBigSwipeDown_sheetCollapsed() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -0.5f
        )

    @Test
    fun sheetGesturesDisabled_userPerformsSwipe_sheetStateDoNotChange() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = 0.5f,
            sheetGesturesEnabled = false
        )

    @Test
    fun sheetHalfExpandedAndNotCollapsable_userPerformsSwipeDown_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = -0.5f,
            collapsable = false
        )

    private fun checkStateAfterSwipe(
        initialState: BottomSheetValue,
        targetState: BottomSheetValue,
        swipeAmount: Float,
        collapsable: Boolean = true,
        sheetGesturesEnabled: Boolean = true
    ) {
        val sheetState = BottomSheetState(initialValue = initialState, collapsable = collapsable)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled
        )
        composeTestRule.onRoot().performSwipe(swipeAmount)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(targetState, currentValue)
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
        height.assertIsEqualTo(min(parentHeight, expectedHeight + getInsets().bottom), "sheet height")
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
        anchorRight.assertIsEqualTo(bottomSheetRight - getInsets().right, "anchor right position")
    }

    @Test
    fun sheetCollapsed_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Expanded)

    private fun checkBottomSheetInsets(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = sheetValue))
        val density = composeTestRule.density
        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val expected = parentHeight - bottomSheet.getBoundsInRoot().top
        val result = (sheetInsets.getBottom(density) / density.density).dp
        result.assertIsEqualTo(expected, "bottom sheet insets")
    }

    @Test
    fun sheetCollapsed_halfExpand_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::halfExpand
        )
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun sheetExpanded_halfExpand_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::halfExpand
        )
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun sheetCollapsed_expand_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::expand
        )
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun sheetHalfExpanded_expand_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::expand
        )
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun sheetExpanded_collapse_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun sheetHalfExpanded_collapse_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun sheetHalfExpandedAndNotCollapsable_collapse_sheetHalfExpanded() {
        val sheetState = BottomSheetState(
            initialValue = BottomSheetValue.HalfExpanded,
            collapsable = false
        )
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetContent = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(contentHeight)
                    )
                },
                sheetPeekHeight = peekHeight,
                sheetHalfExpandedHeight = halfExpandedHeight,
                content = { }
            )
        }

        composeTestRule.onRoot().performSwipe(0.5f)
        composeTestRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        val bottomSheet = composeTestRule.onNode(hasTestTag(BottomSheetTag))
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val sheetTop = bottomSheet.getBoundsInRoot().top
        val height = parentHeight - sheetTop
        val expected = min(parentHeight, contentHeight + getInsets().bottom)

        height.assertIsEqualTo(expected, "sheet height")
    }

    private data class Insets(val left: Dp, val top: Dp, val right: Dp, val bottom: Dp)

    private fun getInsets(): Insets {
        val navigationInsets = composeTestRule.activity.window.decorView.rootWindowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val displayDensity = composeTestRule.activity.resources.displayMetrics.density
        val left = navigationInsets.left / displayDensity
        val top = navigationInsets.top / displayDensity
        val right = navigationInsets.right / displayDensity
        val bottom = navigationInsets.bottom / displayDensity
        return Insets(
            left = left.dp,
            top = top.dp,
            right = right.dp,
            bottom = bottom.dp
        )
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
                sheetInsets = it
            }
        }
    }

    // Swipe for a fraction of the node's height
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
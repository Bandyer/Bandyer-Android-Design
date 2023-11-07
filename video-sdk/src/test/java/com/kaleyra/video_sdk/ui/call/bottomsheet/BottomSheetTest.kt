package com.kaleyra.video_sdk.ui.call.bottomsheet

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.min
import com.kaleyra.video_sdk.call.*
import com.kaleyra.video_sdk.call.bottomsheet.AnchorTag
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetScaffold
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetState
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetTag
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.rememberBottomSheetState
import com.kaleyra.video_sdk.ui.ComposeViewModelsMockTest
import com.kaleyra.video_sdk.ui.performVerticalSwipe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BottomSheetTest : ComposeViewModelsMockTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val peekHeight = 100.dp
    private val halfExpandedHeight = 200.dp
    private val contentHeight = 400.dp

    private var sheetInsets by mutableStateOf(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp))

    @After
    fun tearDown() {
        sheetInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }

    @Test
    fun initialStateHidden_sheetIsHidden() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Hidden)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Hidden, currentValue)
            assert(sheetState.isHidden)
        }
    }

    @Test
    fun initialStateCollapsed_sheetIsCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Collapsed, currentValue)
            assert(sheetState.isCollapsed)
        }
    }

    @Test
    fun initialStateHalfExpanded_sheetIsHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, currentValue)
            assert(sheetState.isHalfExpanded)
        }
    }

    @Test
    fun initialStateExpanded_sheetIsExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Expanded, currentValue)
            assert(sheetState.isExpanded)
        }
    }

    @Test
    fun sheetExpanded_userPerformsBigSwipeDown_sheetHidden() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.Hidden,
            swipeAmount = -0.9f
        )

    @Test
    fun sheetHalfExpanded_userPerformsBigSwipeDown_sheetHidden() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Hidden,
            swipeAmount = -0.5f
        )

    @Test
    fun sheetCollapsed_userPerformsSwipeDown_sheetHidden() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.Hidden,
            swipeAmount = -0.3f
        )

    @Test
    fun sheetCollapsed_userPerformsSmallSwipeUp_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Collapsed,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = 0.2f
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
            swipeAmount = -0.2f
        )

    @Test
    fun sheetExpanded_userPerformsSmallSwipeDown_sheetHalfExpanded() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.HalfExpanded,
            swipeAmount = -0.3f
        )

    @Test
    fun sheetExpanded_userPerformsMediumSwipeDown_sheetCollapsed() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.Expanded,
            targetState = BottomSheetValue.Collapsed,
            swipeAmount = -0.7f
        )

    @Test
    fun sheetHalfExpandedAndNotCollapsable_userPerformsSmallSwipeDown_sheetHidden() =
        checkStateAfterSwipe(
            initialState = BottomSheetValue.HalfExpanded,
            targetState = BottomSheetValue.Hidden,
            swipeAmount = -0.2f,
            collapsable = false
        )

    private fun checkStateAfterSwipe(
        initialState: BottomSheetValue,
        targetState: BottomSheetValue,
        swipeAmount: Float,
        collapsable: Boolean = true
    ) {
        val sheetState = BottomSheetState(initialValue = initialState, isCollapsable = collapsable)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState)
        composeTestRule.onNodeWithTag(BottomSheetTag).performVerticalSwipe(swipeAmount)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(targetState, currentValue)
        }
    }

    @Test
    fun sheetGesturesEnabledTrue_userPerformsSwipe_sheetStateChanged() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState, sheetGestureEnabled = true)
        composeTestRule.onNodeWithTag(BottomSheetTag).performVerticalSwipe(-0.5f)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertNotEquals(BottomSheetValue.Expanded, currentValue)
        }
    }

    @Test
    fun sheetGesturesEnabledFalse_userPerformsSwipe_sheetStateNotChanged() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(sheetState = sheetState, sheetGestureEnabled = false)
        composeTestRule.onNodeWithTag(BottomSheetTag).performVerticalSwipe(-0.5f)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Expanded, currentValue)
        }
    }

    @Test
    fun sheetHidden_sheetIsNotDisplayed() {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = BottomSheetValue.Hidden))
        composeTestRule.onNode(hasTestTag(BottomSheetTag)).assertIsNotDisplayed()
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
        height.assertIsEqualTo(min(parentHeight, expectedHeight), "sheet height")
    }

    @Test
    fun sheetHidden_anchorPositionIsRight() =
        checkAnchorPosition(sheetValue = BottomSheetValue.Hidden)

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
    fun sheetHidden_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Hidden)

    @Test
    fun sheetCollapsed_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Collapsed)

    @Test
    fun sheetHalfExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.HalfExpanded)

    @Test
    fun sheetExpanded_insetsAreCorrect() = checkBottomSheetInsets(BottomSheetValue.Expanded)

    private fun checkBottomSheetInsets(sheetValue: BottomSheetValue) {
        composeTestRule.setBottomSheetScaffold(sheetState = BottomSheetState(initialValue = sheetValue))
        val density = composeTestRule.density
        val bottomSheet = composeTestRule.onNodeWithTag(BottomSheetTag)
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val expected = parentHeight - bottomSheet.getBoundsInRoot().top
        val result = (sheetInsets.getBottom(density) / density.density).dp
        result.assertIsEqualTo(expected, "bottom sheet insets")
    }

    @Test
    fun sheetExpanded_hide_sheetHidden() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::hide
        )
        assertEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun sheetHalfExpanded_hide_sheetHidden() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::hide
        )
        assertEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun sheetCollapsed_hide_sheetHidden() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::hide
        )
        assertEquals(BottomSheetValue.Hidden, sheetState.currentValue)
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
    fun sheetHidden_collapse_sheetCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Hidden)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::collapse
        )
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun sheetHidden_halfExpand_sheetHalfExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Hidden)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::halfExpand
        )
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun sheetHidden_expand_sheetExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Hidden)
        composeTestRule.setBottomSheetScaffold(
            sheetState = sheetState,
            launchedEffect = sheetState::expand
        )
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
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
            isCollapsable = false
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

        composeTestRule.onNodeWithTag(BottomSheetTag).performVerticalSwipe(0.5f)
        composeTestRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        val bottomSheet = composeTestRule.onNodeWithTag(BottomSheetTag)
        val parentHeight = bottomSheet.onParent().getBoundsInRoot().height
        val sheetTop = bottomSheet.getBoundsInRoot().top
        val height = parentHeight - sheetTop
        val expected = min(parentHeight, contentHeight)

        height.assertIsEqualTo(expected, "sheet height")
    }

    private fun ComposeContentTestRule.setBottomSheetScaffold(
        sheetState: BottomSheetState,
        sheetGestureEnabled: Boolean = true,
        launchedEffect: suspend () -> Unit = { }
    ) {
        setContent {
            LaunchedEffect(Unit) {
                launchedEffect.invoke()
            }
            BottomSheetScaffold(
                sheetState = sheetState,
                sheetGesturesEnabled = sheetGestureEnabled,
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
            ) {
                sheetInsets = it
            }
        }
    }

}
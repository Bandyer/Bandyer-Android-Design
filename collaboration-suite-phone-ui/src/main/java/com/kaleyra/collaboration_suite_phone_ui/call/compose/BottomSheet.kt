@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.utils.PreUpPostDownNestedScrollConnection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class BottomSheetValue {
    Collapsed,

    Expanded,

    Hidden,

    HalfExpanded
}

@Stable
class BottomSheetState(
    initialValue: BottomSheetValue,
    val animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    val confirmStateChange: (BottomSheetValue) -> Boolean = { true }
) : SwipeableState<BottomSheetValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {

    val isExpanded: Boolean
        get() = currentValue == BottomSheetValue.Expanded

    val isCollapsed: Boolean
        get() = currentValue == BottomSheetValue.Collapsed

    val isHidden: Boolean
        get() = currentValue == BottomSheetValue.Hidden

    val isHalfExpanded: Boolean
        get() = currentValue == BottomSheetValue.HalfExpanded

    suspend fun expand() = animateTo(BottomSheetValue.Expanded)

    suspend fun collapse() = animateTo(BottomSheetValue.Collapsed)

    suspend fun hide() = animateTo(BottomSheetValue.Hidden)

    suspend fun halfExpand() = animateTo(BottomSheetValue.HalfExpanded)

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (BottomSheetValue) -> Boolean
        ): Saver<BottomSheetState, *> = Saver(
            save = { it.currentValue },
            restore = {
                BottomSheetState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmStateChange = confirmStateChange
                )
            }
        )
    }

    internal val nestedScrollConnection = this.PreUpPostDownNestedScrollConnection
}

@Composable
fun rememberBottomSheetState(
    initialValue: BottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (BottomSheetValue) -> Boolean = { true }
): BottomSheetState {
    return rememberSaveable(
        animationSpec,
        saver = BottomSheetState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    ) {
        BottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    }
}

@Composable
fun BottomSheet(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: BottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed),
    anchor: (@Composable () -> Unit)? = null,
    sheetGesturesEnabled: Boolean = true,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = BottomSheetDefaults.SheetElevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetHalfExpandedHeight: Dp = BottomSheetDefaults.SheetHalfHeight,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier) {
        val fullHeight = constraints.maxHeight.toFloat()
        val peekHeightPx = with(LocalDensity.current) { sheetPeekHeight.toPx() }
        val halfExpandedPx = with(LocalDensity.current) { sheetHalfExpandedHeight.toPx() }
        var bottomSheetHeight by remember { mutableStateOf(fullHeight) }

        val swipeable = Modifier
            .nestedScroll(sheetState.nestedScrollConnection)
            .swipeable(
                state = sheetState,
                anchors = mapOf(
//                    fullHeight to BottomSheetValue.Hidden,
                    fullHeight - peekHeightPx to BottomSheetValue.Collapsed,
                    fullHeight - halfExpandedPx to BottomSheetValue.HalfExpanded,
                    fullHeight - bottomSheetHeight to BottomSheetValue.Expanded
                ),
                orientation = Orientation.Vertical,
                enabled = sheetGesturesEnabled,
                resistance = null
            )
            .semantics {
                if (peekHeightPx != bottomSheetHeight) {
                    if (sheetState.isCollapsed) {
                        expand {
                            if (sheetState.confirmStateChange(BottomSheetValue.Expanded)) {
                                scope.launch { sheetState.expand() }
                            }
                            true
                        }
                    } else {
                        collapse {
                            if (sheetState.confirmStateChange(BottomSheetValue.Collapsed)) {
                                scope.launch { sheetState.collapse() }
                            }
                            true
                        }
                    }
                }
            }

        BottomSheetLayout(
            body = {
                Surface(
                    color = backgroundColor,
                    contentColor = contentColor,
                    content = { content(PaddingValues(bottom = sheetPeekHeight)) }
                )
            },
            bottomSheet = {
                Surface(
                    swipeable
                        .fillMaxWidth()
                        .requiredHeightIn(min = sheetPeekHeight)
                        .onGloballyPositioned {
                            bottomSheetHeight = it.size.height.toFloat()
                        },
                    shape = sheetShape,
                    elevation = sheetElevation,
                    color = sheetBackgroundColor,
                    contentColor = sheetContentColor,
                    content = { Column(content = sheetContent) }
                )
            },
            anchor = {
                Box {
                    anchor?.invoke()
                }
            },
            bottomSheetOffset = sheetState.offset
        )

    }
}

@Composable
private fun BottomSheetLayout(
    body: @Composable () -> Unit,
    bottomSheet: @Composable () -> Unit,
    anchor: @Composable () -> Unit,
    bottomSheetOffset: State<Float>
) {
    Layout(
        content = {
            body()
            bottomSheet()
            anchor()
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)

            val (sheetPlaceable, fabPlaceable) =
                measurables.drop(1).map {
                    it.measure(constraints.copy(minWidth = 0, minHeight = 0))
                }

            val sheetOffsetY = bottomSheetOffset.value.roundToInt()

            sheetPlaceable.placeRelative(0, sheetOffsetY)

            val fabOffsetX = placeable.width - fabPlaceable.width - AnchorEndSpacing.roundToPx()
            val fabOffsetY = sheetOffsetY - fabPlaceable.height - AnchorBottomSpacing.roundToPx()

            fabPlaceable.placeRelative(fabOffsetX, fabOffsetY)
        }
    }
}

private val AnchorBottomSpacing = 16.dp
private val AnchorEndSpacing = 16.dp

object BottomSheetDefaults {

    val SheetElevation = 8.dp

    val SheetPeekHeight = 56.dp

    val SheetHalfHeight = 128.dp
}
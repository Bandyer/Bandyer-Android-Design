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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.utils.PreUpPostDownNestedScrollConnection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal enum class BottomSheetValue {
    Collapsed,

    Expanded,

    HalfExpanded
}

@Stable
internal class BottomSheetState(
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

    val isHalfExpanded: Boolean
        get() = currentValue == BottomSheetValue.HalfExpanded

    suspend fun expand() = animateTo(BottomSheetValue.Expanded)

    suspend fun collapse() = animateTo(BottomSheetValue.Collapsed)

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
internal fun rememberBottomSheetState(
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

internal const val BottomSheetTag = "BottomSheetTag"
internal const val AnchorTag = "AnchorTag"

@Composable
internal fun BottomSheetScaffold(
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
    content: @Composable (WindowInsets) -> Unit
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()
        val peekHeightPx = with(LocalDensity.current) { sheetPeekHeight.toPx() }
        val halfExpandedPx = with(LocalDensity.current) { sheetHalfExpandedHeight.toPx() }
        var bottomSheetHeight by remember { mutableStateOf(fullHeight) }

        val swipeable = Modifier
            .nestedScroll(sheetState.nestedScrollConnection)
            .swipeable(
                state = sheetState,
                anchors = mapOf(
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

        BottomSheetScaffoldLayout(
            body = {
                Surface(
                    color = backgroundColor,
                    contentColor = contentColor,
                    content = { content(sheetPadding(fullHeight, sheetState.offset.value)) }
                )
            },
            bottomSheet = {
                Surface(
                    swipeable
                        .fillMaxWidth()
                        .requiredHeightIn(min = sheetPeekHeight)
                        .testTag(BottomSheetTag)
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
                Box(modifier.testTag(AnchorTag)) { anchor?.invoke() }
            },
            bottomSheetOffset = sheetState.offset
        )

    }
}

private fun sheetPadding(fullHeight: Float, sheetOffset: Float) = object : WindowInsets {
    override fun getBottom(density: Density) = (fullHeight - sheetOffset).roundToInt()
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection) = 0
    override fun getTop(density: Density) = 0
}

@Composable
private fun BottomSheetScaffoldLayout(
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

            val (sheetPlaceable, anchorPlaceable) =
                measurables.drop(1).map {
                    it.measure(constraints.copy(minWidth = 0, minHeight = 0))
                }

            val sheetOffsetY = bottomSheetOffset.value.roundToInt()

            sheetPlaceable.placeRelative(0, sheetOffsetY)

            val anchorOffsetX = placeable.width - anchorPlaceable.width
            val anchorOffsetY = sheetOffsetY - anchorPlaceable.height

            anchorPlaceable.placeRelative(anchorOffsetX, anchorOffsetY)
        }
    }
}

internal object BottomSheetDefaults {

    val SheetElevation = 8.dp

    val SheetPeekHeight = 56.dp

    val SheetHalfHeight = 128.dp
}
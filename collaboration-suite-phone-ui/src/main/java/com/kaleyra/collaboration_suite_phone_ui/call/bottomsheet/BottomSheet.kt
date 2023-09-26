package com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import androidx.compose.ui.unit.*
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.PreUpPostDownNestedScrollConnection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal enum class BottomSheetValue {
    Hidden,
    Collapsed,
    HalfExpanded,
    Expanded
}

@OptIn(ExperimentalMaterialApi::class)
internal class BottomSheetState(
    initialValue: BottomSheetValue,
    val animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    val isCollapsable: Boolean = true,
    val confirmStateChange: (BottomSheetValue) -> Boolean = { true }
) : SwipeableState<BottomSheetValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {

    init {
        if (!isCollapsable) {
            require(initialValue != BottomSheetValue.Collapsed) {
                "The initial value must not be set to Collapsed if collapsable is set to true."
            }
        }
    }

    var minBound = Float.NEGATIVE_INFINITY

    val nestedScrollConnection = this.PreUpPostDownNestedScrollConnection

    val isHidden: Boolean
        get() = currentValue == BottomSheetValue.Hidden

    val isExpanded: Boolean
        get() = currentValue == BottomSheetValue.Expanded

    val isCollapsed: Boolean
        get() = currentValue == BottomSheetValue.Collapsed

    val isHalfExpanded: Boolean
        get() = currentValue == BottomSheetValue.HalfExpanded

    suspend fun expand() = animateTo(BottomSheetValue.Expanded)

    suspend fun collapse() {
        if (!isCollapsable) halfExpand()
        else animateTo(BottomSheetValue.Collapsed)
    }

    suspend fun halfExpand() = animateTo(BottomSheetValue.HalfExpanded)

    suspend fun hide() = animateTo(BottomSheetValue.Hidden)

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            collapsable: Boolean,
            confirmStateChange: (BottomSheetValue) -> Boolean
        ): Saver<BottomSheetState, *> = Saver(
            save = { it.currentValue },
            restore = {
                BottomSheetState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    isCollapsable = collapsable,
                    confirmStateChange = confirmStateChange
                )
            }
        )
    }

}

@Composable
internal fun rememberBottomSheetState(
    initialValue: BottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    collapsable: Boolean = true,
    confirmStateChange: (BottomSheetValue) -> Boolean = { true }
): BottomSheetState {
    return rememberSaveable(
        animationSpec,
        collapsable,
        saver = BottomSheetState.Saver(
            animationSpec = animationSpec,
            collapsable = collapsable,
            confirmStateChange = confirmStateChange
        )
    ) {
        BottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            isCollapsable = collapsable,
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
    sheetHalfExpandedHeight: Dp = BottomSheetDefaults.SheetHalfExpandedHeight,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable (WindowInsets) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()
        val peekHeightPx = with(LocalDensity.current) { sheetPeekHeight.toPx() }
        val halfExpandedPx = with(LocalDensity.current) { sheetHalfExpandedHeight.toPx() }

        BottomSheetScaffoldLayout(
            body = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = backgroundColor,
                    contentColor = contentColor,
                    content = { content(sheetPadding(fullHeight, sheetState.offset)) }
                )
            },
            bottomSheet = {
                BottomSheet(
                    sheetState = sheetState,
                    sheetGesturesEnabled = sheetGesturesEnabled,
                    sheetFullHeight = fullHeight,
                    sheetPeekHeight = peekHeightPx,
                    sheetHalfExpandedHeight = halfExpandedPx,
                    sheetShape = sheetShape,
                    sheetElevation = sheetElevation,
                    sheetBackgroundColor = sheetBackgroundColor,
                    sheetContentColor = sheetContentColor,
                    sheetContent = sheetContent,
                    modifier = Modifier.testTag(BottomSheetTag)
                )
            },
            anchor = {
                Box(modifier = Modifier.testTag(AnchorTag)) {
                    anchor?.invoke()
                }
            },
            sheetOffset = sheetState.offset
        )

    }
}

private fun sheetPadding(fullHeight: Float, sheetOffset: State<Float>) = object : WindowInsets {
    override fun getBottom(density: Density) = (fullHeight - sheetOffset.value).roundToInt()
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection) = 0
    override fun getTop(density: Density) = 0
}

@Composable
private fun BottomSheet(
    sheetState: BottomSheetState,
    sheetGesturesEnabled: Boolean,
    sheetFullHeight: Float,
    sheetPeekHeight: Float,
    sheetHalfExpandedHeight: Float,
    sheetShape: Shape,
    sheetElevation: Dp,
    sheetBackgroundColor: Color,
    sheetContentColor: Color,
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val sheetHeight = remember { mutableStateOf(sheetFullHeight) }

    val swipeable = Modifier
        .nestedScroll(sheetState.nestedScrollConnection)
        .sheetSwipeable(
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled,
            peekHeight = sheetPeekHeight,
            halfExpandedHeight = sheetHalfExpandedHeight,
            fullHeight = sheetFullHeight,
            sheetHeightState = sheetHeight
        )
        .semantics {
            if (sheetPeekHeight != sheetHeight.value) {
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

    val minHeightDp = with(LocalDensity.current) {
        // Set the min height as the half expanded height plus 1 pixel to avoid having the half expanded height equal to the expanded height
        (sheetHalfExpandedHeight + 1).toDp()
    }

    Box(modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = sheetBackgroundColor,
            shape = sheetShape,
            elevation = sheetElevation,
            content = { }
        )
        Surface(
            swipeable
                .fillMaxWidth()
                .requiredHeightIn(min = minHeightDp)
                .onGloballyPositioned {
                    sheetHeight.value = it.size.height.toFloat()
                },
            color = Color.Transparent,
            contentColor = sheetContentColor,
            content = { Column(content = sheetContent) }
        )
    }
}

@Suppress("ModifierInspectorInfo")
@OptIn(ExperimentalMaterialApi::class)
private fun Modifier.sheetSwipeable(
    sheetState: BottomSheetState,
    sheetGesturesEnabled: Boolean,
    peekHeight: Float,
    halfExpandedHeight: Float,
    fullHeight: Float,
    sheetHeightState: State<Float>
): Modifier = composed {
    require(peekHeight <= halfExpandedHeight) {
        "sheet's peek height must be lower than half expanded height."
    }

    val sheetHeight = sheetHeightState.value
    val anchors = if (sheetState.isCollapsable) {
        mapOf(
            fullHeight to BottomSheetValue.Hidden,
            fullHeight - peekHeight to BottomSheetValue.Collapsed,
            fullHeight - halfExpandedHeight to BottomSheetValue.HalfExpanded,
            fullHeight - sheetHeight to BottomSheetValue.Expanded
        )
    } else {
        mapOf(
            fullHeight to BottomSheetValue.Hidden,
            fullHeight - halfExpandedHeight to BottomSheetValue.HalfExpanded,
            fullHeight - sheetHeight to BottomSheetValue.Expanded
        )
    }

    sheetState.minBound = anchors.keys.minOrNull()!!

    swipeable(
        state = sheetState,
        anchors = anchors,
        enabled = sheetGesturesEnabled,
        orientation = Orientation.Vertical,
        resistance = null
    )
}

@Composable
private fun BottomSheetScaffoldLayout(
    body: @Composable () -> Unit,
    bottomSheet: @Composable () -> Unit,
    anchor: @Composable () -> Unit,
    sheetOffset: State<Float>
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

            val sheetOffsetY = sheetOffset.value.roundToInt()

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
    val SheetHalfExpandedHeight = 128.dp
}
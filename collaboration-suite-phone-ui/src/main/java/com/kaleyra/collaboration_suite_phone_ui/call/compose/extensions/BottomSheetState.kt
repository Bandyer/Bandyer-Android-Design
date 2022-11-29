package com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetValue

private const val TargetStateFractionThreshold = .95f
private val FullScreenThreshold = 64.dp

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isNotDraggableDown(): Boolean =
    derivedStateOf { targetValue == BottomSheetValue.Collapsed || (targetValue == BottomSheetValue.HalfExpanded && !collapsable) }.value

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isCollapsed(): Boolean =
    derivedStateOf { targetValue == BottomSheetValue.Collapsed && progress.fraction == 1f }.value

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isCollapsing(): Boolean =
    derivedStateOf { targetValue == BottomSheetValue.Collapsed && progress.fraction >= TargetStateFractionThreshold }.value

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isExitingExpandedState(): Boolean =
    derivedStateOf {
        (currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.HalfExpanded && progress.fraction >= TargetStateFractionThreshold) ||
                (currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Collapsed)
    }.value

internal fun BottomSheetState.isExpandingToFullScreen(density: Density): Boolean =
    derivedStateOf { offset.value < with(density) { FullScreenThreshold.toPx() } }.value
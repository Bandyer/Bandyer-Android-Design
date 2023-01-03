package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetValue

private const val TargetStateFractionThreshold = .99f

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isHidden(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.Hidden && progress.fraction == 1f
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isCollapsed(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.Collapsed && progress.fraction == 1f
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isNotDraggableDown(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.Collapsed || (targetValue == BottomSheetValue.HalfExpanded && !isCollapsable)
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isCollapsing(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.Collapsed && progress.fraction >= TargetStateFractionThreshold
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isHalfExpanding(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.HalfExpanded && progress.fraction >= TargetStateFractionThreshold
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal fun BottomSheetState.isHiding(): State<Boolean> {
    return derivedStateOf {
        targetValue == BottomSheetValue.Hidden && progress.fraction >= TargetStateFractionThreshold
    }
}

internal fun BottomSheetState.isSheetFullScreen(offsetThreshold: Dp, density: Density): State<Boolean> {
    return derivedStateOf {
        offset.value < with(density) { offsetThreshold.toPx() }
    }
}

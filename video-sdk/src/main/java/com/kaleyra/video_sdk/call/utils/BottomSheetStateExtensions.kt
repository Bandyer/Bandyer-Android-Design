package com.kaleyra.video_sdk.call.utils

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetState
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetValue

internal object BottomSheetStateExtensions {

    const val TargetStateFractionThreshold = .99f

    @OptIn(ExperimentalMaterialApi::class)
    fun BottomSheetState.isHidden(): State<Boolean> {
        return derivedStateOf {
            targetValue == BottomSheetValue.Hidden && progress.fraction == 1f
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    fun BottomSheetState.isCollapsed(): State<Boolean> {
        return derivedStateOf {
            targetValue == BottomSheetValue.Collapsed && progress.fraction == 1f
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    fun BottomSheetState.isNotDraggableDown(): State<Boolean> {
        return derivedStateOf {
            targetValue == BottomSheetValue.Collapsed || (targetValue == BottomSheetValue.HalfExpanded && !isCollapsable) || targetValue == BottomSheetValue.Hidden
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    fun BottomSheetState.isCollapsing(): State<Boolean> {
        return derivedStateOf {
            targetValue == BottomSheetValue.Collapsed && progress.fraction >= TargetStateFractionThreshold
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    fun BottomSheetState.isHalfExpanding(): State<Boolean> {
        return derivedStateOf {
            targetValue == BottomSheetValue.HalfExpanded && progress.fraction >= TargetStateFractionThreshold
        }
    }

    fun BottomSheetState.isSheetFullScreen(offsetThreshold: Dp, density: Density): State<Boolean> {
        return derivedStateOf {
            offset.value < with(density) { offsetThreshold.toPx() }
        }
    }

    val BottomSheetState.PreUpPostDownNestedScrollConnection: NestedScrollConnection
        get() = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.toFloat()
                return if (delta < 0 && source == NestedScrollSource.Drag) {
                    performDrag(delta).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (source == NestedScrollSource.Drag) {
                    performDrag(available.toFloat()).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val toFling = Offset(available.x, available.y).toFloat()
                return if (toFling < 0 && offset.value > minBound) {
                    performFling(velocity = toFling)
                    // since we go to the anchor with tween settling, consume all for the best UX
                    available
                } else {
                    Velocity.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                performFling(velocity = Offset(available.x, available.y).toFloat())
                return available
            }

            private fun Float.toOffset(): Offset = Offset(0f, this)

            private fun Offset.toFloat(): Float = this.y
        }
}




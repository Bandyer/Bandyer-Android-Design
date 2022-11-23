package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

internal const val LineTag = "LineTag"

internal val ExpandedLineWidth = 28.dp
internal val CollapsedLineWidth = 4.dp

private val LineHeight = 4.dp

internal sealed class LineState {
    object Expanded : LineState()
    data class Collapsed(val hasBackground: Boolean) : LineState()
}

@Composable
internal fun Line(
    state: LineState,
    onClickLabel: String,
    onClick: () -> Unit
) {
    val contentColor = LocalContentColor.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val width by animateDpAsState(targetValue = if (state is LineState.Collapsed) CollapsedLineWidth else ExpandedLineWidth)
        val color = if (state is LineState.Collapsed && !state.hasBackground) Color.White else contentColor.copy(alpha = 0.8f)

        Spacer(
            modifier = Modifier
                .size(width, LineHeight)
                .background(
                    color = color,
                    shape = CircleShape
                )
                .testTag(LineTag)
        )
    }
}

@Composable
internal fun mapToLineState(sheetState: BottomSheetState): LineState {
    return remember(sheetState) {
        derivedStateOf {
            when {
                isSheetCollapsed(sheetState) -> LineState.Collapsed(hasBackground = false)
                isSheetNotDraggableDown(sheetState) -> LineState.Collapsed(hasBackground = true)
                else -> LineState.Expanded
            }
        }
    }.value
}

@OptIn(ExperimentalMaterialApi::class)
private fun isSheetNotDraggableDown(sheetState: BottomSheetState): Boolean =
    derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed || (sheetState.targetValue == BottomSheetValue.HalfExpanded && !sheetState.collapsable) }.value

@OptIn(ExperimentalMaterialApi::class)
private fun isSheetCollapsed(sheetState: BottomSheetState): Boolean =
    derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f }.value

@Preview
@Composable
internal fun CollapsedLinePreview() {
    KaleyraTheme {
        Line(
            state = LineState.Collapsed(hasBackground = true),
            onClickLabel = "onClickLabel",
            onClick = { }
        )
    }
}

@Preview
@Composable
internal fun CollapsedLineNoBackgroundPreview() {
    KaleyraTheme {
        Line(
            state = LineState.Collapsed(hasBackground = false),
            onClickLabel = "onClickLabel",
            onClick = { }
        )
    }
}

@Preview
@Composable
internal fun ExpandedLinePreview() {
    KaleyraTheme {
        Line(
            state = LineState.Expanded,
            onClickLabel = "onClickLabel",
            onClick = { }
        )
    }
}
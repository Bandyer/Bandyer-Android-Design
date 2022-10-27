@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

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

@Composable
internal fun Line(
    sheetState: BottomSheetState,
    onClickLabel: String,
    onClick: () -> Unit
) {
    val contentColor = LocalContentColor.current
    val isSheetDraggableDown by isSheetDraggableDown(sheetState)
    val isSheetCollapsed by isSheetCollapsed(sheetState)

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
        val width by animateDpAsState(targetValue = if (isSheetDraggableDown) CollapsedLineWidth else ExpandedLineWidth)
        val color = if (isSheetCollapsed) Color.White else contentColor.copy(alpha = 0.8f)

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
private fun isSheetDraggableDown(sheetState: BottomSheetState): State<Boolean> {
    return remember(sheetState) {
        derivedStateOf {
            sheetState.targetValue == BottomSheetValue.Collapsed || (sheetState.targetValue == BottomSheetValue.HalfExpanded && !sheetState.collapsable)
        }
    }
}

@Composable
private fun isSheetCollapsed(sheetState: BottomSheetState): State<Boolean> {
    return remember(sheetState) {
        derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f }
    }
}

@Preview
@Composable
internal fun CollapsedLinePreview() {
    KaleyraTheme {
        Line(
            sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed),
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
            sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.HalfExpanded),
            onClickLabel = "onClickLabel",
            onClick = { }
        )
    }
}
package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
fun Line(
    collapsed: Boolean,
    color: Color,
    onClickLabel: String,
    onClick: () -> Unit,
) {
    val width by animateDpAsState(targetValue = if (collapsed) CollapsedLineWidth else ExpandedLineWidth)
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

@Preview
@Composable
internal fun CollapsedLinePreview() {
    KaleyraTheme {
        Line(
            collapsed = true,
            color = Color.Black,
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
            collapsed = false,
            color = Color.Black,
            onClickLabel = "onClickLabel",
            onClick = { }
        )
    }
}
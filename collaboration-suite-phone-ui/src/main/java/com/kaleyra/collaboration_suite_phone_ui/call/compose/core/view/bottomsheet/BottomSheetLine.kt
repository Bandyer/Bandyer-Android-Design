package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
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
import java.io.Serializable

internal const val LineTag = "LineTag"

internal val ExpandedLineWidth = 28.dp
internal val CollapsedLineWidth = 4.dp

private val LineHeight = 4.dp

// Serializable is needed to save the line state in {@link BottomSheetContentState#Saver}
internal sealed class LineState : Serializable {
    object Expanded : LineState()
    data class Collapsed(val color: Color? = null) : LineState()
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
        val color = if (state is LineState.Collapsed && state.color != null) state.color else contentColor.copy(alpha = 0.6f)

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
internal fun CollapsedLineNoBackgroundPreview() {
    KaleyraTheme {
        Line(state = LineState.Collapsed(Color.White), onClickLabel = "onClickLabel", onClick = { })
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CollapsedLinePreview() {
    KaleyraTheme {
        Surface {
            Line(state = LineState.Collapsed(Color.White), onClickLabel = "onClickLabel", onClick = { })
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ExpandedLinePreview() {
    KaleyraTheme {
        Surface {
            Line(state = LineState.Expanded, onClickLabel = "onClickLabel", onClick = { })
        }
    }
}
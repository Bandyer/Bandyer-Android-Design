package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.supportRtl

private const val DisabledAlpha = 0.25f

@Composable
internal fun IconButton(
    icon: Painter,
    iconDescription: String,
    iconTint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    supportRtl: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        iconTint = iconTint,
        iconSize = iconSize,
        enabled = enabled,
        supportRtl = supportRtl,
        onClick = onClick,
        modifier = modifier
    ) { tint, mod ->
        Icon(
            painter = icon,
            tint = tint,
            contentDescription = iconDescription,
            modifier = mod
        )
    }
}

@Composable
internal fun IconButton(
    icon: ImageVector,
    iconDescription: String,
    iconTint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    supportRtl: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        iconTint = iconTint,
        iconSize = iconSize,
        enabled = enabled,
        supportRtl = supportRtl,
        onClick = onClick,
        modifier = modifier
    ) { tint, mod ->
        Icon(
            imageVector = icon,
            tint = tint,
            contentDescription = iconDescription,
            modifier = mod
        )
    }
}

@Composable
private fun IconButton(
    iconTint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    supportRtl: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (Color, Modifier) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.highlightOnFocus(interactionSource),
        interactionSource = interactionSource
    ) {
        val tint = if (enabled) iconTint else LocalContentColor.current.copy(alpha = DisabledAlpha)
        val mod = Modifier
            .size(iconSize)
            .then(if (supportRtl) Modifier.supportRtl() else Modifier)
        icon(tint, mod)
    }
}
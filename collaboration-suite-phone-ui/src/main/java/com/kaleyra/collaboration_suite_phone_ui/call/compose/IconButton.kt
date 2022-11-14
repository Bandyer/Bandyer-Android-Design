package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
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

// TODO move in a common package for call and chat
@Composable
internal fun IconButton(
    icon: Any,
    iconDescription: String,
    iconTint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    supportRtl: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        when (icon) {
            is Painter -> {
                Icon(
                    painter = icon,
                    tint = tint,
                    contentDescription = iconDescription,
                    modifier = mod
                )
            }
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    tint = tint,
                    contentDescription = iconDescription,
                    modifier = mod
                )
            }
        }
    }
}
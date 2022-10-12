package com.kaleyra.collaboration_suite_phone_ui.chat.topappbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus

private val RippleRadius = 24.dp

@Composable
internal fun MenuIcon(painter: Painter, onClick: () -> Unit, contentDescription: String) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        painter = painter,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = RippleRadius)
            )
            .highlightOnFocus(interactionSource)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = contentDescription
    )
}
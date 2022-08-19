package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
internal fun MenuIcon(painter: Painter, onClick: () -> Unit, contentDescription: String) {
    Icon(
        painter = painter,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = contentDescription
    )
}
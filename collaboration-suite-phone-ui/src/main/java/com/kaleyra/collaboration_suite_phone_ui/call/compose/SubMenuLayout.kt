package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SubMenuLayout(
    title: String,
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.offset(y = (-8).dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            CloseIcon(onClick = onCloseClick)
        }
        content()
    }
}
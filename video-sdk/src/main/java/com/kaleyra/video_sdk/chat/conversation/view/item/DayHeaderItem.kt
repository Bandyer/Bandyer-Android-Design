package com.kaleyra.video_sdk.chat.conversation.view.item

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus

@Composable
internal fun DayHeaderItem(timestamp: Long, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .then(modifier),
        horizontalArrangement = Arrangement.Center
    ) {
        val text = TimestampUtils.parseDay(LocalContext.current, timestamp)
        Text(text = text, fontSize = 12.sp, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onBackground)
    }
}
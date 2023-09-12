package com.kaleyra.collaboration_suite_phone_ui.call.component.pointer.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.rememberCountdownTimerState
import com.kaleyra.collaboration_suite_phone_ui.extensions.TextStyleExtensions.shadow

val PointerSize = 16.dp
const val PointerAutoHideMs = 3000L

@Composable
internal fun TextPointer(
    username: String,
    onTextWidth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = LocalTextStyle.current
    val countDown by rememberCountdownTimerState(initialMillis = PointerAutoHideMs)
    val textAlpha by animateFloatAsState(targetValue =  if (countDown > 0L) 1f else 0f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Pointer()
        Text(
            text = username,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .onGloballyPositioned { onTextWidth(it.size.width) }
                .graphicsLayer { alpha = textAlpha },
            style = textStyle.shadow()
        )
    }
}

@Composable
internal fun Pointer(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .size(PointerSize)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = CircleShape
            )
    )
}
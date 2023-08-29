package com.kaleyra.collaboration_suite_phone_ui.chat.custom

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme

internal val defaultDotSize = 4.dp
internal const val defaultDelay = 300

@Composable
internal fun TypingDots(
    color: Color = MaterialTheme.colors.primary,
    size: Dp = defaultDotSize,
    modifier: Modifier = Modifier
) {
    val maxOffset = 2f

    @Composable
    fun Dot(offset: Float) = Spacer(
        Modifier
            .size(size)
            .offset(y = -offset.dp)
            .background(
                color = color,
                shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateOffsetWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = defaultDelay * 4
                0f at delay with LinearEasing
                maxOffset at delay + defaultDelay with LinearEasing
                0f at delay + defaultDelay * 2
            }
        )
    )

    val offset1 by animateOffsetWithDelay(0)
    val offset2 by animateOffsetWithDelay(defaultDelay)
    val offset3 by animateOffsetWithDelay(defaultDelay * 2)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = maxOffset.dp).then(modifier)
    ) {
        val spaceSize = 2.dp

        Dot(offset1)
        Spacer(Modifier.width(spaceSize))
        Dot(offset2)
        Spacer(Modifier.width(spaceSize))
        Dot(offset3)
    }
}

@Preview
@Composable
internal fun DotsPreview() = KaleyraTheme {
    TypingDots()
}
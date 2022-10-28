package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun CallAction(
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    text: String,
    icon: Painter,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    colors: CallActionColors = CallActionDefaults.colors()
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val backgroundColor by animateColorAsState(
            colors.backgroundColor(toggled = toggled, enabled = enabled).value
        )
        val iconTint by animateColorAsState(
            colors.iconColor(toggled = toggled, enabled = enabled).value
        )
        val rotationValue by animateFloatAsState(rotation)
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .size(CallActionDefaults.Size)
                .toggleable(
                    value = toggled,
                    onValueChange = onToggle,
                    enabled = enabled,
                    role = Role.Checkbox,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(
                        bounded = false,
                        radius = CallActionDefaults.RippleRadius
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier
                    .size(CallActionDefaults.IconSize)
                    .rotate(rotationValue)
            )
        }
        Text(
            text = text,
            color = colors.textColor(enabled = enabled).value,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Stable
internal interface CallActionColors {
    @Composable
    fun backgroundColor(toggled: Boolean, enabled: Boolean): State<Color>

    @Composable
    fun iconColor(toggled: Boolean, enabled: Boolean): State<Color>

    @Composable
    fun textColor(enabled: Boolean): State<Color>
}

internal object CallActionDefaults {

    val Size = 56.dp

    val IconSize = 24.dp

    val RippleRadius = 28.dp

    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = .12f),
        iconColor: Color = contentColorFor(backgroundColor),
        textColor: Color = LocalContentColor.current,
        disabledBackgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = .12f),
        disabledIconColor: Color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
        disabledTextColor: Color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
        toggledBackgroundColor: Color = MaterialTheme.colors.secondaryVariant,
        toggledIconColor: Color = contentColorFor(toggledBackgroundColor)
    ): CallActionColors = DefaultColors(
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        textColor = textColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledIconColor = disabledIconColor,
        disabledTextColor = disabledTextColor,
        toggledBackgroundColor = toggledBackgroundColor,
        toggledIconColor = toggledIconColor
    )
}

@Immutable
private class DefaultColors(
    private val backgroundColor: Color,
    private val iconColor: Color,
    private val textColor: Color,
    private val disabledBackgroundColor: Color,
    private val disabledIconColor: Color,
    private val disabledTextColor: Color,
    private val toggledBackgroundColor: Color,
    private val toggledIconColor: Color,
) : CallActionColors {
    @Composable
    override fun backgroundColor(toggled: Boolean, enabled: Boolean): State<Color> {
        val color = when {
            !enabled -> disabledBackgroundColor
            !toggled -> backgroundColor
            else -> toggledBackgroundColor
        }
        return rememberUpdatedState(color)
    }

    @Composable
    override fun iconColor(toggled: Boolean, enabled: Boolean): State<Color> {
        val color = when {
            !enabled -> disabledIconColor
            !toggled -> iconColor
            else -> toggledIconColor
        }
        return rememberUpdatedState(color)
    }

    @Composable
    override fun textColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) textColor else disabledTextColor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultColors

        if (backgroundColor != other.backgroundColor) return false
        if (iconColor != other.iconColor) return false
        if (textColor != other.textColor) return false
        if (disabledBackgroundColor != other.disabledBackgroundColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (toggledBackgroundColor != other.toggledBackgroundColor) return false
        if (toggledIconColor != other.toggledIconColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundColor.hashCode()
        result = 31 * result + iconColor.hashCode()
        result = 31 * result + textColor.hashCode()
        result = 31 * result + disabledBackgroundColor.hashCode()
        result = 31 * result + disabledIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        result = 31 * result + toggledBackgroundColor.hashCode()
        result = 31 * result + toggledIconColor.hashCode()
        return result
    }
}

@Preview
@Composable
internal fun CallActionPreview() {
    KaleyraTheme {
        PreviewLayout(toggled = false, enabled = true)
    }
}

@Preview
@Composable
internal fun CallActionToggledPreview() {
    KaleyraTheme {
        PreviewLayout(toggled = true, enabled = true)
    }
}

@Preview
@Composable
internal fun CallActionDisabledPreview() {
    KaleyraTheme {
        PreviewLayout(toggled = true, enabled = false)
    }
}

@Composable
private fun PreviewLayout(toggled: Boolean, enabled: Boolean) {
    CallAction(
        toggled = toggled,
        onToggle = { },
        icon = painterResource(id = R.drawable.ic_kaleyra_mic_off),
        rotation = 0f,
        text = stringResource(id = R.string.kaleyra_call_action_mic_mute),
        enabled = enabled
    )
}

package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.view

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

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
        backgroundColor: Color = LocalContentColor.current.copy(alpha = .12f),
        iconColor: Color = contentColorFor(backgroundColor),
        textColor: Color = LocalContentColor.current,
        disabledBackgroundColor: Color = LocalContentColor.current.copy(alpha = .12f),
        disabledIconColor: Color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
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

@Composable
internal fun CallAction(
    action: CallAction,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = action.isEnabled
    val initialToggledValue = (action as? CallAction.Toggleable)?.isToggled ?: false
    var toggled by remember { mutableStateOf(initialToggledValue) }

    val colors = colorsFor(action)
    val backgroundColor by animateColorAsState(
        colors.backgroundColor(toggled = toggled, enabled = enabled).value
    )
    val iconTint by animateColorAsState(
        colors.iconColor(toggled = toggled, enabled = enabled).value
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconToggleButton(
            checked = toggled,
            onCheckedChange = {
                toggled = action is CallAction.Toggleable && it
                onToggle(it)
            },
            enabled = enabled,
            indication = rememberRipple(bounded = false, radius = CallActionDefaults.RippleRadius),
            modifier = Modifier
                .size(CallActionDefaults.Size)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterFor(action),
                contentDescription = descriptionFor(action),
                tint = iconTint,
                modifier = Modifier.size(CallActionDefaults.IconSize)
            )
        }
        Text(
            text = textFor(action),
            color = colors.textColor(enabled = enabled).value,
            fontSize = 12.sp,
            maxLines = 2,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
private fun textFor(action: CallAction): String = stringResource(
    id = when (action) {
        is CallAction.Camera -> R.string.kaleyra_call_action_video_disable
        is CallAction.Microphone -> R.string.kaleyra_call_action_mic_mute
        is CallAction.SwitchCamera -> R.string.kaleyra_call_action_switch_camera
        is CallAction.HangUp -> R.string.kaleyra_call_hangup
        is CallAction.Chat -> R.string.kaleyra_call_action_chat
        is CallAction.Whiteboard -> R.string.kaleyra_call_action_whiteboard
        is CallAction.FileShare -> R.string.kaleyra_call_action_file_share
        is CallAction.Audio -> R.string.kaleyra_call_action_audio_route
        is CallAction.ScreenShare -> R.string.kaleyra_call_action_screen_share
    }
)

@Composable
private fun descriptionFor(action: CallAction) = stringResource(
    id = when (action) {
        is CallAction.Camera -> R.string.kaleyra_call_action_disable_camera_description
        is CallAction.Microphone -> R.string.kaleyra_call_action_disable_mic_description
        is CallAction.SwitchCamera -> R.string.kaleyra_call_action_switch_camera_description
        is CallAction.HangUp -> R.string.kaleyra_call_hangup
        is CallAction.Chat -> R.string.kaleyra_call_action_chat
        is CallAction.Whiteboard -> R.string.kaleyra_call_action_whiteboard
        is CallAction.FileShare -> R.string.kaleyra_call_action_file_share
        is CallAction.Audio -> R.string.kaleyra_call_action_audio_route
        is CallAction.ScreenShare -> R.string.kaleyra_call_action_screen_share
    }
)

@Composable
private fun painterFor(action: CallAction): Painter = painterResource(
    id = when (action) {
        is CallAction.Camera -> R.drawable.ic_kaleyra_camera_off
        is CallAction.Microphone -> R.drawable.ic_kaleyra_mic_off
        is CallAction.SwitchCamera -> R.drawable.ic_kaleyra_switch_camera
        is CallAction.HangUp -> R.drawable.ic_kaleyra_hangup
        is CallAction.Chat -> R.drawable.ic_kaleyra_chat
        is CallAction.Whiteboard -> R.drawable.ic_kaleyra_whiteboard
        is CallAction.FileShare -> R.drawable.ic_kaleyra_file_share
        is CallAction.Audio -> R.drawable.ic_kaleyra_earpiece
        is CallAction.ScreenShare -> R.drawable.ic_kaleyra_screen_share
    }
)

@Composable
private fun colorsFor(action: CallAction): CallActionColors {
    return if (action is CallAction.HangUp) {
        val backgroundColor = colorResource(id = R.color.kaleyra_color_hang_up_button)
        CallActionDefaults.colors(
            backgroundColor = backgroundColor,
            iconColor = Color.White,
            disabledBackgroundColor = backgroundColor.copy(alpha = .12f),
            disabledIconColor = Color.White.copy(alpha = ContentAlpha.disabled)
        )
    } else CallActionDefaults.colors()
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionPreview() {
    CallActionPreview(CallAction.Microphone(isToggled = false, isEnabled = true))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionToggledPreview() {
    CallActionPreview(CallAction.Microphone(isToggled = true, isEnabled = true))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionDisabledPreview() {
    CallActionPreview(CallAction.Microphone(isToggled = true, isEnabled = false))
}

@Composable
private fun CallActionPreview(action: CallAction) {
    KaleyraTheme {
        Surface {
            CallAction(
                action = action,
                onToggle = { }
            )
        }
    }
}

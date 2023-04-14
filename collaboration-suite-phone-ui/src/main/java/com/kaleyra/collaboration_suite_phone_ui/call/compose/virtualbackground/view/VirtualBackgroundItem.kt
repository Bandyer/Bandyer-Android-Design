package com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun VirtualBackgroundItem(
    background: VirtualBackgroundUi,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = when (background) {
                    VirtualBackgroundUi.None -> R.drawable.ic_kaleyra_virtual_background_none
                    VirtualBackgroundUi.Blur -> R.drawable.ic_kaleyra_virtual_background_blur
                    VirtualBackgroundUi.Image -> R.drawable.ic_kaleyra_virtual_background_image
                }
            ),
            contentDescription = null,
            tint = if (selected) MaterialTheme.colors.secondary else LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textFor(background),
            fontSize = 14.sp,
            maxLines = 1,
            color = if (selected) MaterialTheme.colors.secondary else LocalContentColor.current,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
internal fun clickLabelFor(background: VirtualBackgroundUi) = textFor(background = background)

@Composable
private fun textFor(background: VirtualBackgroundUi) =
    stringResource(
        id = when (background) {
            VirtualBackgroundUi.None -> R.string.kaleyra_virtual_background_none
            VirtualBackgroundUi.Blur -> R.string.kaleyra_virtual_background_blur
            VirtualBackgroundUi.Image -> R.string.kaleyra_virtual_background_image
        }
    )

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun NoneBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.None)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun BlurBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.Blur)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ImageBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.Image)
}

@Composable
private fun BackgroundItemPreview(background: VirtualBackgroundUi) {
    KaleyraTheme {
        Surface {
            VirtualBackgroundItem(background = background, false)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SelectedBackgroundItemPreview() {
    KaleyraTheme {
        Surface {
            VirtualBackgroundItem(background = VirtualBackgroundUi.Blur, true)
        }
    }
}
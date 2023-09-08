package com.kaleyra.collaboration_suite_phone_ui.call.screenshare.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

@Composable
internal fun ScreenShareItem(
    screenShareTarget: ScreenShareTargetUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = when (screenShareTarget) {
                    ScreenShareTargetUi.Device -> R.drawable.ic_kaleyra_screen_share_device
                    ScreenShareTargetUi.Application -> R.drawable.ic_kaleyra_screen_share_app
                }
            ),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textFor(screenShareTarget),
            fontSize = 14.sp
        )
    }
}

@Composable
internal fun clickLabelFor(screenShareTarget: ScreenShareTargetUi) =
    textFor(screenShareTarget = screenShareTarget)

@Composable
private fun textFor(screenShareTarget: ScreenShareTargetUi) =
    stringResource(
        id = when (screenShareTarget) {
            ScreenShareTargetUi.Device -> R.string.kaleyra_screenshare_full_device
            ScreenShareTargetUi.Application -> R.string.kaleyra_screenshare_app_only
        }
    )

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareDeviceItemPreview() {
    ScreenShareItemPreview(screenShareTarget = ScreenShareTargetUi.Device)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareAppItemPreview() {
    ScreenShareItemPreview(screenShareTarget = ScreenShareTargetUi.Application)
}

@Composable
private fun ScreenShareItemPreview(screenShareTarget: ScreenShareTargetUi) {
    KaleyraTheme {
        Surface {
            ScreenShareItem(screenShareTarget = screenShareTarget)
        }
    }
}
package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.view.ScreenShareContent
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun ScreenShareScreen(
    items: ImmutableList<ScreenShareTargetUi>,
    onItemClick : (ScreenShareTargetUi) -> Unit,
    onBackPressed: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
        onCloseClick = onBackPressed
    ) {
        ScreenShareContent(
            items = items,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenSharePreview() {
    KaleyraTheme {
        Surface {
            ScreenShareScreen(
                items = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)),
                onItemClick = { },
                onBackPressed = { }
            )
        }
    }
}
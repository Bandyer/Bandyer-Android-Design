package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.submenulayout.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.view.ScreenShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun ScreenShareScreen(
    viewModel: ScreenShareViewModel,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenShareScreen(
        uiState = uiState,
        onItemClick = onItemClick,
        onBackPressed = onBackPressed
    )
}

@Composable
internal fun ScreenShareScreen(
    uiState: ScreenShareUiState,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onBackPressed: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
        onCloseClick = onBackPressed
    ) {
        ScreenShareContent(
            items = uiState.targetList,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareScreenPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareScreen(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onBackPressed = { }
            )
        }
    }
}
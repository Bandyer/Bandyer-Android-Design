package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.view.ScreenShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun ScreenShareSection(
    viewModel: ScreenShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenShareSection(
        uiState = uiState,
        onItemClick = onItemClick,
        onBackPressed = onBackPressed,
        modifier = modifier
    )
}

@Composable
internal fun ScreenShareSection(
    uiState: ScreenShareUiState,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        SubFeatureLayout(
            title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
            onCloseClick = onBackPressed
        ) {
            ScreenShareContent(
                items = uiState.targetList,
                onItemClick = onItemClick
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareSectionPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareSection(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onBackPressed = { }
            )
        }
    }
}
package com.kaleyra.collaboration_suite_phone_ui.call.screenshare

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.view.ScreenShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun ScreenShareComponent(
    viewModel: ScreenShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScreenShareViewModel.provideFactory(::requestConfiguration)
    ),
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onClick = remember {
        { target: ScreenShareTargetUi ->
            when (target) {
                ScreenShareTargetUi.Application -> viewModel.shareApplicationScreen(context)
                ScreenShareTargetUi.Device -> viewModel.shareDeviceScreen(context)
            }
            onItemClick(target)
        }
    }
    ScreenShareComponent(
        uiState = uiState,
        onItemClick = onClick,
        onCloseClick = onCloseClick,
        modifier = modifier
    )
}

@Composable
internal fun ScreenShareComponent(
    uiState: ScreenShareUiState,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
        onCloseClick = onCloseClick,
        modifier = modifier
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
internal fun ScreenShareComponentPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareComponent(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
package com.kaleyra.video_sdk.call.virtualbackground

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.model.mockVirtualBackgrounds
import com.kaleyra.video_sdk.call.virtualbackground.view.VirtualBackgroundContent
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.theme.KaleyraTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.R

@Composable
internal fun VirtualBackgroundComponent(
    viewModel: VirtualBackgroundViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = VirtualBackgroundViewModel.provideFactory(::requestConfiguration)
    ),
    onItemClick: (VirtualBackgroundUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onClick = remember {
        { background: VirtualBackgroundUi ->
            viewModel.setEffect(background)
            onItemClick(background)
        }
    }
    VirtualBackgroundComponent(
        uiState = uiState,
        onItemClick = onClick,
        onCloseClick = onCloseClick,
        modifier = modifier
    )
}

@Composable
internal fun VirtualBackgroundComponent(
    uiState: VirtualBackgroundUiState,
    onItemClick: (VirtualBackgroundUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_virtual_background_picker_title),
        onCloseClick = onCloseClick,
        modifier = modifier
    ) {
        VirtualBackgroundContent(
            items = uiState.backgroundList,
            currentBackground = uiState.currentBackground,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun VirtualBackgroundComponentPreview() {
    KaleyraTheme {
        Surface {
            VirtualBackgroundComponent(
                uiState = VirtualBackgroundUiState(backgroundList = mockVirtualBackgrounds),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
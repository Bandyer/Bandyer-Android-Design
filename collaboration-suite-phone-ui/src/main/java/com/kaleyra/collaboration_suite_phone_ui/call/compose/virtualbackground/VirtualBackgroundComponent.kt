package com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.view.VirtualBackgroundContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

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
            items = uiState.backgrounds,
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
                uiState = VirtualBackgroundUiState(backgrounds = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id"), VirtualBackgroundUi.Image("id2")))),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
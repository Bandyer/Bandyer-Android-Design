package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.view.AudioOutputContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

@Composable
internal fun AudioOutputComponent(
    viewModel: AudioOutputViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AudioOutputViewModel.provideFactory(::requestConfiguration)
    ),
    onDeviceConnected: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AudioOutputComponent(
        uiState = uiState,
        onItemClick = remember(scope, viewModel, onDeviceConnected) {
            {
                scope.coroutineContext.cancelChildren()
                scope.launch {
                    viewModel.setDevice(it)
                    onDeviceConnected()
                }
            }
        },
        onCloseClick = onCloseClick,
        modifier = modifier
    )
}

@Composable
internal fun AudioOutputComponent(
    uiState: AudioOutputUiState,
    onItemClick: (AudioDeviceUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_audio_route_title),
        onCloseClick = onCloseClick,
        modifier = modifier
    ) {
        AudioOutputContent(
            items = uiState.audioDeviceList,
            playingDeviceId = uiState.playingDeviceId,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputComponentPreview() {
    KaleyraTheme {
        Surface {
            AudioOutputComponent(
                uiState = AudioOutputUiState(
                    audioDeviceList = mockAudioDevices,
                    playingDeviceId = mockAudioDevices.value[0].id
                ),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
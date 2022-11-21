package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.view.AudioOutputContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun AudioOutputScreen(
    viewModel: AudioOutputViewModel,
    onItemClick: (AudioDevice) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AudioOutputScreen(
        uiState = uiState,
        onItemClick = onItemClick,
        onBackPressed = onBackPressed
    )
}

@Composable
internal fun AudioOutputScreen(
    uiState: AudioOutputState,
    onItemClick: (AudioDevice) -> Unit,
    onBackPressed: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_audio_route_title),
        onCloseClick = onBackPressed
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
internal fun AudioOutputScreenPreview() {
    KaleyraTheme {
        Surface {
            AudioOutputScreen(
                uiState = AudioOutputState(audioDeviceList = mockAudioDevices, playingDeviceId = mockAudioDevices.value[0].id),
                onItemClick = { },
                onBackPressed = { }
            )
        }
    }
}
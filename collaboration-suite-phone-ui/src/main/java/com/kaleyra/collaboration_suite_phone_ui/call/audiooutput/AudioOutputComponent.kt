package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.view.AudioOutputContent
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BluetoothConnectPermission
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BluetoothScanPermission
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

@ChecksSdkIntAtLeast(api = 34)
private val ShouldAskBluetoothPermissionImmediately = Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
private val ShouldAskBluetoothPermissionOnClick = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun AudioOutputComponent(
    viewModel: AudioOutputViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AudioOutputViewModel.provideFactory(::requestConfiguration)
    ),
    onDeviceConnected: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(permissions = listOf(BluetoothScanPermission, BluetoothConnectPermission))
    } else null

    if (ShouldAskBluetoothPermissionImmediately && !isTesting) {
        LaunchedEffect(Unit) {
            permissionsState?.launchMultiplePermissionRequest()
        }
    }

    AudioOutputComponent(
        uiState = uiState,
        onItemClick = remember(viewModel, onDeviceConnected) {
            {
                if (it is AudioDeviceUi.Bluetooth && ShouldAskBluetoothPermissionOnClick && !isTesting) {
                    permissionsState?.launchMultiplePermissionRequest()
                }
                viewModel.setDevice(it)
                onDeviceConnected()
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
package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.view.CallActionsContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.RecordAudioPermission
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
internal fun CallActionsComponent(
    viewModel: CallActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallActionsViewModel.provideFactory(::requestConfiguration)
    ),
    permissionsState: MultiplePermissionsState? = null,
    onItemClick: (action: CallAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity() as? FragmentActivity
    var recordAudioPermissionLaunched by remember { mutableStateOf(false) }
    var cameraPermissionLaunched by remember { mutableStateOf(false) }
    val recordAudioPermission by remember(permissionsState) {
        derivedStateOf {
            permissionsState?.permissions?.firstOrNull { it.permission == RecordAudioPermission }
        }
    }
    val cameraPermission by remember(permissionsState) {
        derivedStateOf {
            permissionsState?.permissions?.firstOrNull { it.permission == CameraPermission }
        }
    }

    if (activity != null) {
        LaunchedEffect(recordAudioPermission) {
            snapshotFlow { recordAudioPermission?.status }
                .filterNotNull()
                .onEach {
                    if (it.isGranted && recordAudioPermissionLaunched) viewModel.startMicrophone(activity)
                    recordAudioPermissionLaunched = false
                }
                .launchIn(this)
        }

        LaunchedEffect(cameraPermission) {
            snapshotFlow { cameraPermission?.status }
                .filterNotNull()
                .onEach {
                    if (it.isGranted && cameraPermissionLaunched) viewModel.startCamera(activity)
                    cameraPermissionLaunched = false
                }
                .launchIn(this)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallActionsComponent(
        uiState = uiState,
        onItemClick = { action ->
            // TODO revise this
            when (action) {
                is CallAction.Microphone -> {
                    if (recordAudioPermission?.status != PermissionStatus.Granted) {
                        recordAudioPermissionLaunched = true
                        recordAudioPermission?.launchPermissionRequest()
                    } else {
                        viewModel.toggleMic()
                    }
                }
                is CallAction.Camera -> {
                    if (cameraPermission?.status != PermissionStatus.Granted) {
                        cameraPermissionLaunched = true
                        cameraPermission?.launchPermissionRequest()
                    } else {
                        viewModel.toggleCamera()
                    }
                }
                is CallAction.SwitchCamera -> viewModel.switchCamera()
                is CallAction.HangUp -> viewModel.hangUp()
                is CallAction.ScreenShare -> {
                    if (!viewModel.tryStopScreenShare()) {
                        onItemClick(action)
                    }
                }
                is CallAction.Chat -> activity?.let { viewModel.showChat(it.baseContext) }
                else -> onItemClick(action)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun CallActionsComponent(
    uiState: CallActionsUiState,
    onItemClick: (action: CallAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        CallActionsContent(
            items = uiState.actionList,
            itemsPerRow = uiState.actionList.count().coerceIn(1, 4),
            onItemClick = onItemClick
        )
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsComponentPreview() {
    KaleyraTheme {
        Surface {
            CallActionsComponent(
                uiState = CallActionsUiState(actionList = mockCallActions),
                onItemClick = { }
            )
        }
    }
}
package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_extension_audio.extensions.AudioOutputConnectionError
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.AudioOutputGenericFailureSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.AudioOutputInSystemCallFailureSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.CameraRestrictionSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.MutedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingEndedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingErrorSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingStartedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbConnectedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbDisconnectedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.UsbNotSupportedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.CameraRestrictionMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UsbCameraMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

const val RecordingStarted = "RecordingStarted"
const val RecordingStopped = "RecordingStopped"
const val RecordingError = "RecordingError"
const val UsbConnected = "UsbConnected"
const val UsbDisconnected = "UsbDisconnected"
const val UsbNotSupported = "UsbNotSupported"
const val CameraRestriction = "CameraRestriction"
const val AudioOutputGenericFailure = "AudioOutputGenericFailure"
const val AudioOutputInSystemCallFailure = "AudioOutputInSystemCallFailure"
const val MutedByAdmin = "MutedByAdmin"

// TODO move to common package between call and chat
@Composable
internal fun UserMessageSnackbarHandler(
    userMessage: UserMessage?,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedState by rememberUpdatedState(userMessage)
    LaunchedEffect(snackbarHostState) {
        snapshotFlow { updatedState }
            .filterNotNull()
            .buffer()
            .collect {
                snackbarHostState.showSnackbar(
                    message = when (it) {
                        is MutedMessage -> it.admin ?: ""
                        is UsbCameraMessage.Connected -> it.name
                        else -> ""
                    },
                    actionLabel = when (it) {
                        is RecordingMessage.Started -> RecordingStarted
                        is RecordingMessage.Stopped -> RecordingStopped
                        is RecordingMessage.Failed -> RecordingError
                        is UsbCameraMessage.Connected -> UsbConnected
                        is UsbCameraMessage.Disconnected -> UsbDisconnected
                        is UsbCameraMessage.NotSupported -> UsbNotSupported
                        is CameraRestrictionMessage -> CameraRestriction
                        is AudioConnectionFailureMessage.Generic -> AudioOutputGenericFailure
                        is AudioConnectionFailureMessage.InSystemCall -> AudioOutputInSystemCallFailure
                        is MutedMessage -> MutedByAdmin
                        else -> null
                    }
                )
            }
    }

    SnackbarHost(
        modifier = modifier.padding(vertical = 12.dp),
        hostState = snackbarHostState,
        snackbar = {
            when (it.actionLabel) {
                RecordingStarted -> RecordingStartedSnackbar()
                RecordingStopped -> RecordingEndedSnackbar()
                RecordingError -> RecordingErrorSnackbar()
                UsbConnected -> UsbConnectedSnackbar(it.message)
                UsbDisconnected -> UsbDisconnectedSnackbar()
                UsbNotSupported -> UsbNotSupportedSnackbar()
                CameraRestriction -> CameraRestrictionSnackbar()
                AudioOutputGenericFailure -> AudioOutputGenericFailureSnackbar()
                AudioOutputInSystemCallFailure -> AudioOutputInSystemCallFailureSnackbar()
                MutedByAdmin -> MutedSnackbar(it.message)
            }
        }
    )
}
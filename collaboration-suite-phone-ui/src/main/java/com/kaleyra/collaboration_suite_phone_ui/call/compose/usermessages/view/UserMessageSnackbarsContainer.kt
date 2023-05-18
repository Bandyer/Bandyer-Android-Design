package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.MutedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingEndedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingErrorSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingStartedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages

@Composable
internal fun UserMessageSnackbarsContainer(
    userMessages: UserMessages,
    modifier: Modifier = Modifier,
    recordingSnackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    mutedSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Column(modifier = modifier.padding(12.dp)) {
        if (userMessages.recordingMessage != null) {
            LaunchedEffect(userMessages.recordingMessage, recordingSnackBarHostState) {
                recordingSnackBarHostState.showSnackbar("")
            }
        }

        if (userMessages.mutedMessage != null) {
            LaunchedEffect(userMessages.mutedMessage, mutedSnackbarHostState) {
                mutedSnackbarHostState.showSnackbar("")
            }
        }

        SnackbarHost(
            hostState = recordingSnackBarHostState,
            snackbar = {
                when (userMessages.recordingMessage) {
                    is RecordingMessage.Started -> RecordingStartedSnackbar()
                    is RecordingMessage.Stopped -> RecordingEndedSnackbar()
                    is RecordingMessage.Failed -> RecordingErrorSnackbar()
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SnackbarHost(
            hostState = mutedSnackbarHostState,
            snackbar = { userMessages.mutedMessage?.let { MutedSnackbar(it.admin) } }
        )
    }
}
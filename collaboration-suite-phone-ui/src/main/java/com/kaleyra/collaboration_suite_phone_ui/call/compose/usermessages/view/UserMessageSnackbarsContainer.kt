package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage

const val RecordingStarted = "RecordingStarted"
const val RecordingEnded = "RecordingEnded"
const val RecordingError = "RecordingError"

// TODO move to common package between call and chat
@Composable
internal fun UserMessageSnackbarsContainer(
    modifier: Modifier = Modifier,
    recordingUserMessage: RecordingMessage? = null,
    mutedUserMessage: MutedMessage? = null,
    recordingSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    mutedSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    if (recordingUserMessage != null) {
        LaunchedEffect(recordingUserMessage, recordingSnackbarHostState) {
            recordingSnackbarHostState.showSnackbar(
                when (recordingUserMessage) {
                    is RecordingMessage.Started -> RecordingStarted
                    is RecordingMessage.Stopped -> RecordingEnded
                    else -> RecordingError
                }
            )
        }
    }

    if (mutedUserMessage != null) {
        LaunchedEffect(mutedUserMessage, mutedSnackbarHostState) {
            mutedSnackbarHostState.showSnackbar(mutedUserMessage.admin ?: "")
        }
    }

    Column(modifier) {
        SnackbarHost(
            hostState = recordingSnackbarHostState,
            snackbar = {
                when (it.message) {
                    RecordingStarted -> RecordingStartedSnackbar()
                    RecordingEnded -> RecordingEndedSnackbar()
                    else -> RecordingErrorSnackbar()
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SnackbarHost(
            hostState = mutedSnackbarHostState,
            snackbar = { MutedSnackbar(it.message) }
        )
    }
}
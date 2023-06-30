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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.MutedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingEndedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingErrorSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.RecordingStartedSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull

const val RecordingStarted = "RecordingStarted"
const val RecordingStopped = "RecordingStopped"
const val RecordingError = "RecordingError"
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
                        else -> ""
                    },
                    actionLabel = when (it) {
                        is RecordingMessage.Started -> RecordingStarted
                        is RecordingMessage.Stopped -> RecordingStopped
                        is RecordingMessage.Failed -> RecordingError
                        else -> MutedByAdmin
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
                MutedByAdmin -> MutedSnackbar(it.message)
            }
        }
    )
}
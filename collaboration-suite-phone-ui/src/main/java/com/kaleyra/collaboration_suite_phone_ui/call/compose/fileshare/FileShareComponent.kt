package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick.FilePickActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick.FilePickBroadcastReceiver
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.MaxFileSizeDialog
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.view.UserMessageSnackbarsContainer
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareComponent(
    viewModel: FileShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FileShareViewModel.provideFactory(configure = ::requestConfiguration, filePickProvider = FilePickBroadcastReceiver)
    ),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val (showUnableToOpenFileSnackBar, setShowUnableToOpenSnackBar) = remember { mutableStateOf(false) }
    val (showCancelledFileSnackBar, setShowCancelledFileSnackBar) = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordingUserMessage by viewModel.recordingUserMessage.collectAsStateWithLifecycle(initialValue = null)
    val mutedUserMessage by viewModel.mutedUserMessage.collectAsStateWithLifecycle(initialValue = null)

    DisposableEffect(context) {
        context.sendBroadcast(Intent(context, FileShareVisibilityObserver::class.java).apply {
            action = FileShareVisibilityObserver.ACTION_FILE_SHARE_DISPLAYED
        })
        onDispose {
            context.sendBroadcast(Intent(context, FileShareVisibilityObserver::class.java).apply {
                action = FileShareVisibilityObserver.ACTION_FILE_SHARE_NOT_DISPLAYED
            })
        }
    }

    FileShareComponent(
        uiState = uiState,
        showUnableToOpenFileSnackBar = showUnableToOpenFileSnackBar,
        showCancelledFileSnackBar = showCancelledFileSnackBar,
        onFileOpenFailure = { doesFileExists ->
            if (doesFileExists) setShowUnableToOpenSnackBar(true)
            else setShowCancelledFileSnackBar(true)
        },
        onSnackBarShowed = {
            setShowUnableToOpenSnackBar(false)
            setShowCancelledFileSnackBar(false)
        },
        onAlertDialogDismiss = viewModel::dismissUploadLimit,
        onUpload = viewModel::upload,
        onDownload = viewModel::download,
        onShareCancel = viewModel::cancel,
        modifier = modifier,
        recordingUserMessage = recordingUserMessage,
        mutedUserMessage = mutedUserMessage
    )
}

@Composable
internal fun FileShareComponent(
    uiState: FileShareUiState,
    showUnableToOpenFileSnackBar: Boolean = false,
    showCancelledFileSnackBar: Boolean = false,
    onFileOpenFailure: ((doesFileExists: Boolean) -> Unit)? = null,
    onSnackBarShowed: (() -> Unit)? = null,
    onAlertDialogDismiss: (() -> Unit)? = null,
    recordingUserMessage: RecordingMessage? = null,
    mutedUserMessage: MutedMessage? = null,
    onUpload: (Uri) -> Unit,
    onDownload: (String) -> Unit,
    onShareCancel: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current

    val showFilePicker = remember { { FilePickActivity.show(context) } }

    val unableToOpenFileText = stringResource(id = R.string.kaleyra_fileshare_impossible_open_file)
    val fileCancelledText = stringResource(id = R.string.kaleyra_fileshare_file_cancelled)

    val onItemClick = remember {
        { file: SharedFileUi ->
            if (file.state is SharedFileUi.State.Success) {
                val uri = if (file.isMine) file.uri.value else file.state.uri.value
                context.tryToOpenFile(uri) { onFileOpenFailure?.invoke(it) }
            }
        }
    }

    if (showUnableToOpenFileSnackBar) {
        LaunchedEffect(unableToOpenFileText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = unableToOpenFileText)
            onSnackBarShowed?.invoke()
        }
    }

    if (showCancelledFileSnackBar) {
        LaunchedEffect(fileCancelledText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = fileCancelledText)
            onSnackBarShowed?.invoke()
        }
    }

    if (uiState.showFileSizeLimit) {
        MaxFileSizeDialog(onDismiss = { onAlertDialogDismiss?.invoke() })
    }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.sharedFiles.count() < 1) {
                FileShareEmptyContent(modifier = Modifier.matchParentSize())
            } else {
                FileShareContent(
                    items = uiState.sharedFiles,
                    onItemClick = onItemClick,
                    onItemActionClick = {
                        when(it.state) {
                            SharedFileUi.State.Available -> onDownload(it.id)
                            SharedFileUi.State.Pending, is SharedFileUi.State.InProgress -> onShareCancel(it.id)
                            is SharedFileUi.State.Success -> onItemClick(it)
                            SharedFileUi.State.Error -> if (it.isMine) onUpload(it.uri.value) else onDownload(it.id)
                            else -> Unit
                        }
                    },
                    modifier = Modifier.matchParentSize()
                )
            }

            UserMessageSnackbarsContainer(
                recordingUserMessage = recordingUserMessage,
                mutedUserMessage = mutedUserMessage
            )

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            )

            FileShareFab(
                collapsed = uiState.sharedFiles.count() > 0,
                onClick = showFilePicker,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareComponentPreview() {
    KaleyraTheme {
        Surface {
            FileShareComponent(
                uiState = FileShareUiState(),
                onUpload = {},
                onDownload = {},
                onShareCancel = {}
            )
        }
    }
}
